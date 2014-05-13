package com.idiot2ger.beluga.dbtools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.idiot2ger.beluga.dbtools.model.BaseModel;
import com.idiot2ger.beluga.dbtools.model.TableModel;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TemplateManager {

  static Configuration mTemplateConfig = null;

  private static void init() {
    if (mTemplateConfig == null) {
      mTemplateConfig = new Configuration();
      mTemplateConfig.setTemplateLoader(new ClassTemplateLoader(TemplateManager.class, "template"));
      mTemplateConfig.setDefaultEncoding("UTF-8");
    }
  }

  private static String createFilePath(BaseModel model, String baseFolder) {
    String basePath = baseFolder;
    String pkgPath = model.getClassPackageName().replaceAll("\\.", "/");

    File parentFile = null;
    if (basePath == null) {
      parentFile = new File(pkgPath);
    } else {
      parentFile = new File(basePath, pkgPath);
    }
    parentFile.mkdirs();
    return parentFile.getAbsolutePath() + "\\" + model.getClassName() + ".java";
  }

  private static void runTemplate(BaseModel model, String baseFolder, String templateName) {
    init();
    String path = createFilePath(model, baseFolder);
    System.out.println(path);
    try {
      Template template = mTemplateConfig.getTemplate(templateName);
      template.process(model, new PrintWriter(path));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TemplateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }


  public static void runTableClassTemplate(TableModel model, String baseFolder) {
    runTemplate(model, baseFolder, "TableClass.ftl");
  }
}
