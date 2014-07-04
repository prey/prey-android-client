package com.prey.json.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.prey.PreyLogger;
import com.prey.actions.file.FilePrey;
import com.prey.actions.observer.ActionResult;
import com.prey.actions.retrieval.FileRetrievalClient;
import com.prey.net.PreyWebServices;
import com.prey.net.http.EntityFile;

public class FileRetrieval {

	
	public void start(Context ctx, List<ActionResult> list, JSONObject parameters) {
		
		try {
			String host = parameters.getString("host");
			String port = parameters.getString("port");
			FileRetrievalClient client=new FileRetrievalClient();
			client.connect(host, port);
		} catch (Exception e) {
			PreyLogger.i("Error, causa:"+e.getMessage());
		}
		
	}
	public void get(Context ctx, List<ActionResult> list, JSONObject parameters) {
		String folderParam = null;
		try {
			folderParam = parameters.getString("folder");
		} catch (Exception e) {

		}
		PreyLogger.i("folderParam:" + folderParam);
		if (folderParam != null && !"".equals(folderParam)) {
			getFolder(ctx, folderParam);
		} else {
			try {
				String fileParam = parameters.getString("file");
				PreyLogger.i("fileParam:" + fileParam);
				getFile(ctx, fileParam);
			} catch (Exception e) {

			}

		}

	}
	
	public void del(Context ctx, List<ActionResult> list, JSONObject parameters) {
		try {
			String fileParam = parameters.getString("file");
			PreyLogger.i("fileParam:" + fileParam);
			String base = "" + Environment.getExternalStorageDirectory();
			String filename=base+"/"+fileParam;
			PreyLogger.i("filename:" + filename);
			File file = new File(filename);
			file.delete();
		} catch (Exception e) {

		}
	}

	public void getFile(Context ctx, String fileParam) {
		InputStream is = null;
		try {
			String base = "" + Environment.getExternalStorageDirectory();
			String filename=base+"/"+fileParam;
			PreyLogger.i("filename:" + filename);
			File file = new File(filename);
			int size=(int)file.length();
			PreyLogger.i("size:"+size);
			is = new FileInputStream(file);
			EntityFile entityFile = new EntityFile();
			entityFile.setFile(is);
			entityFile.setMimeType("image/png");
			entityFile.setName("file");
			entityFile.setType("file");
			entityFile.setLength((int) file.length());

			PreyWebServices.getInstance().sendFileRetrieval(ctx, entityFile);
		} catch (Exception ex) {
			PreyLogger.e("Error:" + ex.getMessage(), ex);

		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception e) {
			}
		}
	}

	public void getFolder(Context ctx, String folderParam) {
		String folder = "";
		if ("all".equals(folderParam)) {
			folder = "" + Environment.getExternalStorageDirectory();
		}
		File dir = new File(folder);
		List<FilePrey> files = getFilesRecursive(folder, dir);
		JSONArray array = new JSONArray();
		int i = 0;
		array = createJSON(i, array, files);
		HashMap<String, String> parametersMap = createMap(array);
		try {
			PreyWebServices.getInstance().sendJsonRetrieval(ctx, parametersMap);
		} catch (Exception ex) {
			PreyLogger.e("Error:" + ex.getMessage(), ex);

		}
	}

	public HashMap<String, String> createMap(JSONArray array) {

		HashMap<String, String> parametersMap = new HashMap<String, String>();
		for (int i = 0; array != null && i < array.length(); i++) {
			try {
				JSONObject json = (JSONObject) array.get(i);
				String file = json.getString("file");
				parametersMap.put("file_retrieval[" + i + "][name]", json.getString("name"));
				parametersMap.put("file_retrieval[" + i + "][path]", json.getString("path"));
				parametersMap.put("file_retrieval[" + i + "][file]", file);
				if ("1".equals(file)) {
					parametersMap.put("file_retrieval[" + i + "][size]", json.getString("size"));
				}
			} catch (Exception e) {
			}
		}

		return parametersMap;
	}

	private JSONArray createJSON(int pos, JSONArray array, List<FilePrey> files) {

		for (int i = 0; files != null && i < files.size(); i++) {
			FilePrey file = files.get(i);
			JSONObject json = new JSONObject();
			try {
				json.put("name", file.getFilename());
				json.put("path", file.getPath());
				if (file.isFile()) {
					json.put("size", file.getSize());
				}
				json.put("file", file.isFile() ? 1 : 0);
			} catch (Exception e) {
			}
			PreyLogger.i("[" + pos + "]:" + json.toString());
			pos++;
			array.put(json);
			if (!file.isFile()) {
				array.put(createJSON(pos, array, file.getChilds()));
			}
		}
		return array;
	}

	private List<FilePrey> getFilesRecursive(String pathBase, File folder) {
		init();
		List<FilePrey> files = new ArrayList<FilePrey>();
		for (File child : folder.listFiles()) {
			String parent = child.getParent().replace(pathBase, "");
			FilePrey file = new FilePrey();
			file.setFilename(child.getName());

			if (child.isDirectory()) {

				List<FilePrey> listChilds = getFilesRecursive(pathBase, child);
				if (listChilds != null && listChilds.size() > 0) {
					// PreyLogger.i("d:"+parent.trim()+"/"+child.getName().trim());
					file.setChilds(listChilds);
					file.setPath(parent);
					file.setFile(false);
					files.add(file);
				}
			}
			if (child.isFile()) {

				String extension = MimeTypeMap.getFileExtensionFromUrl(child.getName());
				if (isValid(extension)) {
					// PreyLogger.i("f:" + child.getName());
					MimeTypeMap mime = MimeTypeMap.getSingleton();
					file.setMimetype(mime.getMimeTypeFromExtension(extension));
					file.setSize(child.length());
					file.setPath(parent);
					file.setFile(true);
					files.add(file);
				}

			}

		}
		return files;
	}

	private void init() {
		listExtensions = new ArrayList<String>();
		// listExtensions.add("JPG");
		listExtensions.add("MP3");
		listExtensions.add("GIF");
		listExtensions.add("PDF");
		listExtensions.add("PNG");
		listExtensions.add("DOC");
		listExtensions.add("ZIP");
		listExtensions.add("RAR");
		listExtensions.add("MP4");
		listExtensions.add("AVI");
		listExtensions.add("MIDI");
		// listExtensions.add("JPEG");
	}

	private List<String> listExtensions = null;

	private boolean isValid(String string) {
		if (string == null)
			return false;
		return listExtensions.contains(string.toUpperCase());
	}
}
