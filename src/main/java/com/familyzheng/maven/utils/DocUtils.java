package com.familyzheng.maven.utils;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DocUtils {

    public static void  publishZip(String currentDir,Model model) throws IOException {
        FileOutputStream fos = new FileOutputStream(currentDir+"/"+model.getArtifactId()+"-"+model.getVersion()+".zip");
        ZipOutputStream zos = new ZipOutputStream(fos);
        List<String> strings = new ArrayList<>();
        strings.add(String.join("/",model.getGroupId().split("\\.")));
        strings.add(model.getArtifactId());
        strings.add(model.getVersion());
        String pack =  String.join("/",strings);
        File file = new File(currentDir);
        for (File listFile : file.listFiles()) {
            if(listFile.isFile()&&listFile.getName().startsWith(model.getArtifactId()+"-"+model.getVersion())&&!listFile.getName().endsWith(".zip")){
                addToZip(listFile,pack, zos);
            }
        }
        zos.close();
        fos.close();
    }

    public static void  javadoc(String path,Model model) throws IOException {
        String fileName = model.getArtifactId()+"-"+model.getVersion()+"-javadoc.jar";
        FileOutputStream fos = new FileOutputStream(path+"target/"+fileName);
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", model.getVersion());
        manifest.getMainAttributes().putValue("Built-By", model.getName());
        JarOutputStream jos = new JarOutputStream(fos, manifest);
        jos.close();
        fos.close();
    }
    public static void  source(String path,Model model) throws IOException {
        String fileName = model.getArtifactId()+"-"+model.getVersion()+"-sources.jar";
        FileOutputStream fos = new FileOutputStream(path+"target/"+fileName);
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", model.getVersion());
        manifest.getMainAttributes().putValue("Built-By", model.getName());
        JarOutputStream jos = new JarOutputStream(fos, manifest);
        addToJar(path+"src/main/java",path+"src/main/java",jos);
        jos.close();
        fos.close();
    }


    private static void addToJar(String root,String filePath,JarOutputStream jos) throws IOException {
        File file = new File(filePath);
        if(file.isFile()){
            JarEntry jarEntry = new JarEntry(filePath.substring(root.length()+1));
            jos.putNextEntry(jarEntry);
            try (FileInputStream fis = new FileInputStream(filePath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    jos.write(buffer, 0, bytesRead);
                }
            }
            jos.closeEntry();
        }else {
            File[] files = file.listFiles();
            for (File file1 : files) {
                addToJar(root,file1.getPath(),jos);
            }
        }
    }
    private static void addToZip(File file,String pack, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(pack+"/"+file.getName());
            zos.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        }
    }
}
