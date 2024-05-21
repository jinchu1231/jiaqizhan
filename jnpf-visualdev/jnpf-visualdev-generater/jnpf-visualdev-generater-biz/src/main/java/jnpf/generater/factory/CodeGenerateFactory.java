package jnpf.generater.factory;

import jnpf.base.util.functionForm.CodeGenerateUtil;
import jnpf.base.util.functionForm.FlowFormUtil;
import jnpf.base.util.functionForm.FormListUtil;
import jnpf.base.util.functionForm.FormUtil;
import jnpf.base.util.functionForm.FunctionFlowUtil;
import jnpf.generater.model.FormDesign.TemplateMethodEnum;
import org.springframework.stereotype.Component;

/**
 * 代码生成工厂类
 *
 * @author JNPF开发平台组
 * @version V3.2
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/10/8
 */
@Component
public class CodeGenerateFactory {

	/**
	 * 根据模板路径对应实体
	 * @param templateMethod
	 * @return
	 */
	public CodeGenerateUtil getGenerator(String templateMethod){
		if (templateMethod.equals(TemplateMethodEnum.T2.getMethod())){
			return  FormListUtil.getFormListUtil();
		}else if (templateMethod.equals(TemplateMethodEnum.T4.getMethod())){
			return  FormUtil.getFormUtil();
		}else if (templateMethod.equals(TemplateMethodEnum.T3.getMethod())){
			return  FunctionFlowUtil.getFunctionFlowUtil();
		}else if (templateMethod.equals(TemplateMethodEnum.T5.getMethod())){
			return  FlowFormUtil.getFormUtil();
		}else {
			return null;
		}
	}
}
