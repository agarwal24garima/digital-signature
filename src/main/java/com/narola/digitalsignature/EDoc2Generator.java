/*
 * Copyright (c) VAS "Latvijas Valsts radio un televīzijas centrs" (http://www.eparaksts.lv)
 * Developed by SIA "EUSO" (http://www.euso.lv)
 */
package com.narola.digitalsignature;


import lv.eparaksts.A.N;
import lv.eparaksts.edoc.*;
import lv.eparaksts.helpers.QualifiedSignatureHelper;
import lv.pasts.eme.security.CertificateEntry;
import lv.pasts.eme.security.KeyAccessor;
import lv.pasts.eme.smartcard.CardAccessor;
import lv.pasts.eme.smartcard.CardAccessorCallback;
import lv.pasts.eme.tsp.TimeStamp;
import lv.pasts.eme.tsp.TimeStampGenerator;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


import java.nio.file.Files;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/*
 * EDOC 2.0 sample application.
 * This example demonstrates how to create a signed EDOC 2.0 format document.
 *
 * Example creates EDOC 2.0 object, adds data file 'args[0]',
 * signs EDOC document if password is provided as a parameter 'args[2]'
 * and finally writes EDOC document to file 'args[1]'.
 */
public class EDoc2Generator implements CardAccessorCallback {

	private char[] password;

	public static void main(String[] args){
		try {
			generateSignedDocument(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void generateSignedDocument(String[] args) throws Exception {

			//collect parameters
		String fileToAdd = args[0];
		String edocToWrite = args[1];
		char[] password = args[3].toCharArray();

		//
		EDoc2 edoc = null;
		FileInputStream fileInputStream = null;
		FileOutputStream edocOutputStream = null;

		try {
			//create edoc
			System.out.println("Begin...");
			System.out.println(System.getenv("PATH"));
			System.out.println(System.getProperty("java.library.path"));
			//System.out.println(System.getProperty("java.library.path"));
			edoc = EDoc2Builder.newEDoc();
			System.out.println("1 - EDOC object created");

			//add file
			File file = new File(fileToAdd);
			fileInputStream = new FileInputStream(file);
			@SuppressWarnings("Since15") String mimeType = Files.probeContentType(file.toPath());
			if (mimeType == null) mimeType = "application/octet-stream";
			edoc.addDataObject(fileInputStream, file.getName(), mimeType);
			System.out.println("2 - File " + fileToAdd + " added");

			//check password
			if (password != null) {
				//prepare KeyAccessor for signature
				EDoc2Generator cardAccessorCallback = new EDoc2Generator();
				cardAccessorCallback.password = password;
				KeyAccessor keyAccessor = CardAccessor.getInstance(cardAccessorCallback);

				//prepare certificate
				String signingAlias = keyAccessor.selectDocumentSigningCertificate();
				X509Certificate certificate = keyAccessor.getCertificate(signingAlias);

				/*
				 * Signature PHASE 1 - prepare signature [PrivateKey IS NOT required]
				 */

				//prepare signature
				Map<String, String> signatureProperties = new HashMap<String, String>();
				signatureProperties.put(EDoc2Signature.KEY_SIGNATURE_METHOD_URI, EDoc2Signature.SIGNATURE_METHOD_RSA_SHA256);
				EDoc2BasicSignature basicSignature = edoc.addSignature(signatureProperties);
				basicSignature.setSignatureProductionPlace(new SignatureProductionPlace("Riga"));
				basicSignature.setSignerClaimedRoles(Collections.singletonList("Manager"));
				basicSignature.setSigningCertificate(certificate);
				System.out.println("3.1 - Signature prepared");

				//get signable data
				byte[] signable = basicSignature.getSignableBytes();
				String signatureMethodAlgorithm = basicSignature.getSignatureMethodAlgorithm();

				/*
				 * NOTE: At this moment edoc optionally can be stored to file
				 * and opened later to support server side load balance scenario.
				 */

				/*
				 * Signature PHASE 2 - calculate signature [PrivateKey IS required]
				 */

				//sign data, optionally use an external process
				Signature signature = Signature.getInstance(signatureMethodAlgorithm);
				signature.initSign(keyAccessor.getPrivateKey(signingAlias));
				signature.update(signable);
				byte[] signatureValue = signature.sign();

				//set signature value
				basicSignature.setSignatureValue(signatureValue);
				/*
				 * NOTE: At this moment signature has to be extended with
				 * timestamp and revocation data to conform QUALIFIED profile.
				 */
				boolean requestTimeStamp = true; //false to avoid billing
				if (requestTimeStamp) {
					cardAccessorCallback.password = args[2].toCharArray();
					keyAccessor = CardAccessor.getInstance(cardAccessorCallback);

					//prepare certificate
					signingAlias = keyAccessor.selectDocumentSigningCertificate();
					certificate = keyAccessor.getCertificate(signingAlias);

					EDoc2QualifiedSignature qualifiedSignature = basicSignature.qualifiedSignature();
					byte[] signatureValueDigest = qualifiedSignature.getSignatureValueDigest("SHA-256");
					TimeStamp timeStamp = TimeStampGenerator.requestTimeStamp(TimeStampGenerator.DIGEST_ALGORITHM_SHA256,
							signatureValueDigest, keyAccessor);
					qualifiedSignature.setSignatureTimestamps(Collections.singletonList(timeStamp.getEncoded()));
					boolean useTrustedList = false; //whether to use EU Trusted Status List data
					QualifiedSignatureHelper helper = new QualifiedSignatureHelper(certificate, timeStamp, useTrustedList);
					qualifiedSignature.setCertificateValues(helper.getCertificates());
					qualifiedSignature.setRevocationValues(helper.getOCSPResponses(), helper.getCRLResponses());
				}

				//complete signature
				basicSignature.complete();
				System.out.println("3.3 - Signature completed");

			} else {
				System.out.println("3 - Signature not added, smartcard access password not provided");
			}

			//save edoc
			edocOutputStream = new FileOutputStream(edocToWrite);
			edoc.writeTo(edocOutputStream);
			System.out.println("4 - EDOC file written to " + edocToWrite);

		} catch (Exception exc) {
			exc.printStackTrace();

		} finally {
			//close fileInputStream
			if (fileInputStream != null) fileInputStream.close();

			//close edocOutputStream
			if (edocOutputStream != null) edocOutputStream.close();

			//close edoc
			if (edoc != null) edoc.close();
		}

		//done
		System.out.println("Finished");
		System.exit(0);
	}

	public char[] getCardPIN(String tokenSerial, String tokenLabel, long sessionId, X509Certificate cert, boolean isRetry) {
		System.out.println("getCardPIN: " + tokenLabel + ", cert: " + cert.getSubjectX500Principal().toString());
		if (isRetry) {
			//PIN was incorrect, cancel
			System.err.println("getCardPIN: PIN incorrect, exit!");
			return null;
		}

		System.out.println("using PIN: " + new String(password));
		return password;
	}

	public CertificateEntry selectDocumentSigningCertificate(CertificateEntry[] certList) {
		if (certList.length > 0) {
			return certList[0];
		}

		return null;
	}

	public CertificateEntry selectClientAuthenticationCertificate(CertificateEntry[] certList) {
		if (certList.length > 0) {
			return certList[0];
		}

		return null;
	}

}
