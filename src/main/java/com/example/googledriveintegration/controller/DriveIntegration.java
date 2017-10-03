package com.example.googledriveintegration.controller;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.netflix.infix.lang.infix.antlr.EventFilterParser.boolean_expr_return;

/**
 * DRIVE KEY: AIzaSyCOTxgSxul7htlsEgMmkWe2CZWsx_F-cZg SERVER KEY:
 * AIzaSyA7MOTsytu3g4sZ9jsYSxnzRzNa8rFm_iM
 * 
 * @author Anand Singh <email: avsmips@gmail.com>
 *
 */
@RestController
public class DriveIntegration {
	/** Application name. */
	private static final String APPLICATION_NAME = "Default Demo App";
	// "Drive API Java Quickstart"

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(
			System.getProperty("user.home"),
			".credentials/drive-java-quickstart");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final com.google.api.client.json.JsonFactory JSON_FACTORY = JacksonFactory
			.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/**
	 * Global instance of the scopes required by this quickstart.
	 *
	 * If modifying these scopes, delete your previously saved credentials at
	 * ~/.credentials/drive-java-quickstart
	 */
	private static final List<String> SCOPES = Arrays.asList(
			DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE);

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	public static Credential authorize() throws IOException {
		// Load client secrets.
		InputStream in = DriveIntegration.class
				.getResourceAsStream("/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
				JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, DriveScopes.all())
				.setDataStoreFactory(DATA_STORE_FACTORY)
				.setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		System.out.println("Credentials saved to "+ DATA_STORE_DIR.getAbsolutePath());
		return credential;
	}

	/**
	 * Build and return an authorized Drive client service.
	 * 
	 * @return an authorized Drive client service
	 * @throws IOException
	 */
	public static Drive getDriveService() throws IOException {
		Credential credential = authorize();
		return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}

	/*
	 * public static void main(String[] args) throws IOException { // Build a
	 * new authorized API client service. Drive service = getDriveService();
	 * 
	 * // Print the names and IDs for up to 10 files. FileList result =
	 * service.files().list().setPageSize(10)
	 * .setFields("nextPageToken, files(id, name)").execute(); List<File> files
	 * = result.getFiles(); if (files == null || files.size() == 0) {
	 * System.out.println("No files found."); } else {
	 * System.out.println("Files:"); for (File file : files) {
	 * System.out.printf("%s (%s)\n", file.getName(), file.getId()); } } }
	 */

	@GetMapping(value = "/drive/list", produces = MediaType.APPLICATION_JSON)
	public List<File> listFiles(@RequestParam(value="pageSize", defaultValue="10") int pageSize) throws IOException {
		// Build a new authorized API client service.
		Drive service = getDriveService();
		// Print the names and IDs for up to 10 files.
		FileList result = service
				.files()
				.list()
				.setPageSize(pageSize)
				.setFields("nextPageToken, files(id, name, mimeType, modifiedTime)")
				.execute();
		List<File> files = result.getFiles();

		return files;
	}

	@GetMapping(value = "/drive/search", produces = MediaType.APPLICATION_JSON)
	public Map<String, String> searchFiles() throws IOException {
		// Build a new authorized API client service.
		Drive service = getDriveService();
		Map<String, String> fileList = new HashedMap();
		// Print the names and IDs for up to 10 files.
		FileList result = service.files().list().setPageSize(10)
				.setFields("nextPageToken, files(id, name)").execute();
		List<File> files = result.getFiles();
		if (files == null || files.size() == 0) {
			System.out.println("No files found.");
		} else {
			System.out.println("Files:");
			for (File file : files) {
				System.out.printf("%s (%s)\n", file.getName(), file.getId());
				fileList.put(file.getId(), file.getName());
			}
		}
		return fileList;
	}

	@GetMapping(value = "/drive/download", produces = MediaType.APPLICATION_JSON)
	public FileList downloadFileWithId(
			@RequestParam(value = "fileName", required=false) String fileName, @RequestParam(value="fileId", required=true) String fileId)
			throws IOException {
		// Build a new authorized API client service.
		Drive service = getDriveService();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// Print the names and IDs for up to 10 files.
		FileList result = service.files().list()
				.setQ("name = '" + fileName + "'")
				.setFields("nextPageToken, files(id, name, mimeType)")
				.execute();

		List<File> files = result.getFiles();
		if (files == null || files.size() == 0) {
			System.out.println("No files found.");
		} else {
			System.out.println("Files:");
			for (File file : files) {
				System.out.printf("%s (%s)\n", file.getName(), file.getId());

				//download the file with fileId
				service.files().get(file.getId()).executeMediaAndDownloadTo(baos);
				
				// save the output stream to disk
				FileOutputStream fos = new FileOutputStream("u:/temp/"
						+ file.getId() + ".jpeg");
				baos.writeTo(fos);
				baos.flush();
				baos.close();
				fos.close();
			}
		}
		return result;
	}

	@ExceptionHandler(value = { Exception.class })
	public String exceptions(Exception ex) {
		return ex.getMessage();
	}
}
