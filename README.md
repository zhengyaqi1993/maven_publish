# maven仓库发布插件

该插件支持用户便捷删除上传中央仓库压缩文件

1、简略javadoc文件生成  
2、简化sources文件生成  
3、处理pom文件生成  
4、支持MD5加密、sha1加密、pgp加密  
5、规范打包成可直接上传maven仓库的Zip文件

说明：使用方式
```
<plugin>
    <groupId>com.familyzheng.maven</groupId>
    <artifactId>publish</artifactId>
    <version>1.0</version>
    <executions>
        <execution>
            <goals>
                <goal>run</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
如不想使用缺省私钥，可通过增加配置方式调整私钥密码
```
<configuration>
    <passphrase>密码</passphrase>
    <keyPath>私钥路径</keyPath>
</configuration>
```