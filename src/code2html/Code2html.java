package code2html;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
/**
 *
 * @author zsh
 */
public class Code2html {
    public static int STATE_TEXT = 1; // 普通文本
	public static int STATE_DOUBLE_QUOTE = 2; // 双引号
	public static int STATE_SINGLE_QUOTE = 3; // 单引号
	public static int STATE_MULTI_LINE_COMMENT = 4; // 多行注释
	public static int STATE_LINE_COMMENT = 5; // 单行注释

	private int lineNumber; // 行号
	private boolean enableLineNumber = true; // 开启行号标志

	public boolean isEnableLineNumber() {
		return enableLineNumber;
	}

	
	public void setEnableLineNumber(boolean enableLineNumber) {
		this.enableLineNumber = enableLineNumber;
	}

	String[] literalArray = { "null", "true", "false" }; // 字面常量
	String[] keywordArray = { "abstract", "break", "case", "catch", "class",
			"const", "continue", "default", "do", "else", "extends", "final",
			"finally", "for", "goto", "if", "implements", "import",
			"instanceof", "interface", "native", "new", "package", "private",
			"protected", "public", "return", "static", "strictfp", "super",
			"switch", "synchronized", "this", "throw", "throws", "transient",
			"try", "volatile", "while" }; // 关键词
	String[] primitiveTypeArray = { "boolean", "char", "byte", "short", "int",
			"long", "float", "double", "void" }; // 原始数据类型

	Set<String> literalSet = new HashSet<String>(Arrays.asList(literalArray));
	Set<String> keywordSet = new HashSet<String>(Arrays.asList(keywordArray));
	Set<String> primitiveTypeSet = new HashSet<String>(Arrays
			.asList(primitiveTypeArray));

	public String process(String src) {
		int currentState = STATE_TEXT;
		int identifierLength = 0;
		int currentIndex = -1;
		char currentChar = 0;
		String identifier = "";
		StringBuffer out = new StringBuffer();
		src = src.replaceAll(">", "&gt;").replaceAll("<", "&lt;");

		while (++currentIndex != src.length() - 1) {
			if (currentIndex == 0) {
				out.insert(out.length(),
						"<span class=\"lineNumberStyle\">1.</span>");
				lineNumber++;

			}
			currentChar = src.charAt(currentIndex);
			if (Character.isJavaIdentifierPart(currentChar)) {
				out.append(currentChar);
				++identifierLength;
				continue;
			}
			if (identifierLength > 0) {
				identifier = out.substring(out.length() - identifierLength);
				if (currentState == STATE_TEXT) {
					if (literalSet.contains(identifier)) { 
						out.insert(out.length() - identifierLength,
								"<span class=\"literalStyle\">");
						out.append("</span>");
					} else if (keywordSet.contains(identifier)) {
						out.insert(out.length() - identifierLength,
								"<span class=\"keywordStyle\">");
						out.append("</span>");
					} else if (primitiveTypeSet.contains(identifier)) { 
						out.insert(out.length() - identifierLength,
								"<span class=\"primitiveTypeStyle\">");
						out.append("</span>");
					} else if (identifier.equals(identifier.toUpperCase())
							&& !Character.isDigit(identifier.charAt(0))) { 
						out.insert(out.length() - identifierLength,
								"<span class=\"constantStyle\">");
						out.append("</span>");
					} else if (Character.isUpperCase(identifier.charAt(0))) { 
						out.insert(out.length() - identifierLength,
								"<span class=\"nonPrimitiveTypeStyle\">");
						out.append("</span>");
					}
				}
			}

			switch (currentChar) {
			case '\"':
				out.append('\"');
				if (currentState == STATE_TEXT) {
					currentState = STATE_DOUBLE_QUOTE;
					out.insert(out.length() - ("\"").length(),
							"<span class=\"doubleQuoteStyle\">");
				} else if (currentState == STATE_DOUBLE_QUOTE) {
					currentState = STATE_TEXT;
					out.append("</span>");
				}
				break;
			case '\'':
				out.append("\'");
				if (currentState == STATE_TEXT) {
					currentState = STATE_SINGLE_QUOTE;
					out.insert(out.length() - ("\'").length(),
							"<span class=\"singleQuoteStyle\">");
				} else if (currentState == STATE_SINGLE_QUOTE) {
					currentState = STATE_TEXT;
					out.append("</span>");
				}
				break;
			case '\\':
				out.append("\\");
				if (currentState == STATE_DOUBLE_QUOTE
						|| currentState == STATE_SINGLE_QUOTE) {
					out.append(src.charAt(++currentIndex));
				}
				break;
			case '*':
				out.append('*');
				if (currentState == STATE_TEXT && currentIndex > 0
						&& src.charAt(currentIndex - 1) == '/') {
					out.insert(out.length() - ("/*").length(),
							"<span class=\"multiLineCommentStyle\">");
					currentState = STATE_MULTI_LINE_COMMENT;
				}
				break;
			case '/':
				out.append("/");
				if (currentState == STATE_TEXT && currentIndex > 0
						&& src.charAt(currentIndex - 1) == '/') {
					out.insert(out.length() - ("//").length(),
							"<span class=\"singleLineCommentStyle\">");
					currentState = STATE_LINE_COMMENT;
				} else if (currentState == STATE_MULTI_LINE_COMMENT) {
					out.append("</span>");
					currentState = STATE_TEXT;
				}
				break;

			case '\t':// 遇到\t无条件插入4个空格
				out.insert(out.length(), "&nbsp;&nbsp;&nbsp;&nbsp;");
				break;

			case '\r':
			case '\n':
				// end single line comments
				if (currentState == STATE_LINE_COMMENT) {
					out.insert(out.length(), "</span>");
					currentState = STATE_TEXT;
				}
				if (currentChar == '\r' && currentIndex < src.length() - 1) {
					++currentIndex;
				} else
					out.append('\n');

				if (enableLineNumber)
					out.append("<br/><span class=\"lineNumberStyle\">"
							+ (++lineNumber) + ".</span>");
				break;
			case 0:
				if (currentState == STATE_LINE_COMMENT
						&& currentIndex == (src.length() - 1))
					out.append("</span>");
				break;
			default: // everything else
				out.append(currentChar);
			}
			identifierLength = 0;
		}
		return "<html><head><style >*{font-weight: bold;}.container{margin:0 auto;width:960px;border:1px solid gray;}.doubleQuoteStyle{color: #008;}.keywordStyle{color: #606; }.multiLineCommentStyle ,.singleLineCommentStyle{ color: #080; }.lineNumberStyle{background-color: #eee; color: gray;}</style></head><body><div class=\"container\">"
				+ out.toString() + "</div></body></html>";
	}
    
    public static void main(String args[]) throws Exception {
		File file = new File("G://test.java");
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF-8"));
		StringBuffer sb = new StringBuffer();
		String temp = null;
		while ((temp = br.readLine()) != null) {
			sb.append(temp).append('\n');
			
		}
		String src = sb.toString();
		Code2html jsh = new Code2html();
		System.out.println(jsh.process(src));
	}
}