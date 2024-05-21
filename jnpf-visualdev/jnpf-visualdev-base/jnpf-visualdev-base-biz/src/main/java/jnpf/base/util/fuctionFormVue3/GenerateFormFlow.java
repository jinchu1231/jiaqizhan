package jnpf.base.util.fuctionFormVue3;


import jnpf.base.util.fuctionFormVue3.common.GenerateInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JNPF开发平台组
 * @version V3.6.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/5/31
 */
public class GenerateFormFlow implements GenerateInterface {
    @Override
    public List<String> getTemplates(String templatePath, int type, boolean hasImport) {
        List<String> templates = new ArrayList<>();
        //前端
        templates.add(templatePath + File.separator + "html" + File.separator + "index.vue.vm");
        templates.add(templatePath + File.separator + "html" + File.separator + "Form.vue.vm");
        //api接口
        templates.add(File.separator + "helper" + File.separator + "api.ts.vm");
        //后端
        templates.add(File.separator + "java" + File.separator + "Form.java.vm");
        templates.add(File.separator + "java" + File.separator + "Constant.java.vm");
        return templates;
    }

    @Override
    public List<String> getChildTemps(boolean isChild) {
        List<String> templates = new ArrayList<>();
        if (isChild) {
            templates.add(File.separator + "java" + File.separator + "Model.java.vm");
        }
        return templates;
    }

}
