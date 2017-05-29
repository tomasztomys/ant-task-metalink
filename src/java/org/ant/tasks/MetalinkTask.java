package org.ant.tasks;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MetalinkTask extends Task {

	 
	private String file;
    private String location;
    private String url;
    private List<File> files = new ArrayList<>();
    private Vector<FileSet> filesets = new Vector();
    
    private String dirPath = "";

    public void setUrl(String url) {
    	this.url = url;
    }
    
    public String getUrl() {
    	return this.url;
    }
    
    public void setFile(String file) {
        this.file = file;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }
    
    private void addFile(String path) {
    	System.out.println(path);
		File file = new File(path);
		this.files.add(file);
	}
    
    private String convertMd5BytesToString(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
    
    private String getMD5(File file) throws NoSuchAlgorithmException, IOException {
    	 byte[] digest = null;
         digest = MessageDigest.getInstance("MD5").digest(Files.readAllBytes(file.toPath()));
         return this.convertMd5BytesToString(digest);
  
    }
    
    private void createMetalinkXml() throws ParserConfigurationException, TransformerException {
    	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("metalink");
		rootElement.setAttribute("version", "1.0");
		rootElement.setAttribute("xmlns", "urn:ietf:params:xml:ns:metalink");
		doc.appendChild(rootElement);
		
		Element published = doc.createElement("published");
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
		df.setTimeZone(tz);
		String nowAsISO = df.format(new Date());
		published.appendChild(doc.createTextNode(nowAsISO));
		rootElement.appendChild(published);
		
		this.files.forEach((file) -> {
			Element fileElement = doc.createElement("file");
			
			Attr fileName = doc.createAttribute("name");
			fileName.setValue(file.getName());
			fileElement.setAttributeNode(fileName);			
			
			Element fileSize = doc.createElement("size");
			fileSize.appendChild(doc.createTextNode(Long.toString(file.length())));
			fileElement.appendChild(fileSize);
			
			
			
			Element hash = doc.createElement("hash");
			Attr hashType = doc.createAttribute("type");
			hashType.setValue("MD5");
			hash.setAttributeNode(hashType);	
			try {
				hash.appendChild(doc.createTextNode(this.getMD5(file)));
			} catch (DOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileElement.appendChild(hash);
			
			
			Element fileUrl = doc.createElement("url");
			fileUrl.appendChild(doc.createTextNode(this.url + file.getPath().replace('\\', '/').replace(this.dirPath, "")));
			fileElement.appendChild(fileUrl);
			
			
			rootElement.appendChild(fileElement);
		});
		
		
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(this.file));

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		transformer.transform(source, result);

		System.out.println("File saved!");

    }
	 
	@Override
	public void execute() throws BuildException {
			if(this.url == null) {
				this.setUrl(getProject().getProperty("server.files.url"));
			}
		
		  
			    FileSet fs = filesets.get(0);
			    DirectoryScanner dir = fs.getDirectoryScanner();
			 
			    String[] srcs = dir.getIncludedFiles();
			    for (int j = 0; j < srcs.length; j++) {
			   
						this.dirPath = fs.getDir().getPath().replace('\\', '/')+"/";
				
				      this.addFile(dirPath + srcs[j]);	   
			    }
		  
		  
		try {
	
			this.createMetalinkXml();
		
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
