package com.familyzheng.maven;

import com.familyzheng.maven.utils.DocUtils;
import com.familyzheng.maven.utils.SignUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.bouncycastle.openpgp.PGPException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

@Mojo(name = "run", defaultPhase = LifecyclePhase.PACKAGE)
@Execute(phase = LifecyclePhase.PACKAGE)
public class MainMojo extends AbstractMojo {

    @Parameter(property = "passphrase", defaultValue = "")
    private String passphrase;
    @Parameter(property = "keyPath", defaultValue = "key.asc")
    private String keyPath;


    public void execute() {
        try {
            String path = System.getProperty("user.dir")+"/";
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(path + "pom.xml"));
            String fileName = model.getArtifactId()+"-"+model.getVersion();
            //1 清理
            File file = new File(path+"target");
            for (File listFile : file.listFiles()) {
                if(listFile.getName().startsWith(model.getArtifactId()+"-"+model.getVersion())){
                    listFile.delete();
                }
            }
            System.out.println("[INFO] 代码清理完成");
            //2 相关文件生成
            Path sourcePath = Paths.get("pom.xml");
            Path targetPath = Paths.get("target/"+fileName+".pom");
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[INFO] pom生成完成");
            DocUtils.javadoc(path,model);
            System.out.println("[INFO] Java文档生成完成");
            DocUtils.source(path,model);
            System.out.println("[INFO] 源文件生成完成");
            for (File listFile : file.listFiles()) {
                if(listFile.getName().startsWith(model.getArtifactId()+"-"+model.getVersion())){
                    String asc = SignUtils.signFile(listFile.getPath(),keyPath,passphrase);
                    Files.write(Paths.get(listFile.getPath()+".asc"), Collections.singleton(asc));
                    String md5 = SignUtils.getFileHash(listFile.getPath(), "MD5");
                    Files.write(Paths.get(listFile.getPath()+".md5"), Collections.singleton(md5));
                    String sha1 = SignUtils.getFileHash(listFile.getPath(), "SHA-1");
                    Files.write(Paths.get(listFile.getPath()+".sha1"), Collections.singleton(sha1));
                }
            }
            System.out.println("[INFO] 签名完成");
            DocUtils.publishZip(file.getPath(),model);
            System.out.println("[INFO] 打包完成");
        } catch (IOException | XmlPullParserException | PGPException e) {
            e.printStackTrace();
        }
    }
}