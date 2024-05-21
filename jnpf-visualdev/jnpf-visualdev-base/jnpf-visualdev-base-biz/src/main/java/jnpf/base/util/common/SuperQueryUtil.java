package jnpf.base.util.common;

import jnpf.emnus.ModuleTypeEnum;
import jnpf.util.XSSEscape;
import lombok.Cleanup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class SuperQueryUtil {

	public static void CreateJsFile(String data,String path,String jsFileType) throws IOException {
		path = XSSEscape.escapePath(path);
		String content = "const "+jsFileType+" = " + data;
		File jsFile = new File(path);
		Writer writer = null;
		try {
			writer = new FileWriter(jsFile);
			writer.write(content);
			writer.write(System.getProperty("line.separator"));
			writer.write("export default "+jsFileType);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer!=null){
				writer.close();
			}
		}
	}

	public static void CreateFlowFormJsonFile(String data, String path){
		try {
			File file = new File(XSSEscape.escapePath(path+File.separator+"flowForm."+ ModuleTypeEnum.FLOW_FLOWDFORM.getTableName()));
			boolean b = file.createNewFile();
			if(b) {
				@Cleanup Writer out = new FileWriter(file);
				out.write(data);
				out.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}


}
