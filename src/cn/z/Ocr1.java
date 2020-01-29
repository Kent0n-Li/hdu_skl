package cn.z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.jhlabs.image.ScaleFilter;
import org.apache.http.util.EntityUtils;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;

import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import cn.z.svm.svm_predict;
import cn.z.util.CommonUtil;
import sun.misc.BASE64Decoder;

import static java.lang.Integer.parseInt;

public class Ocr1 extends Thread{

	private static String clazz = Ocr1.class.getSimpleName();
	private static int whiteThreshold = 540;
	private static boolean useSvm = true;
	private static int ThreadNum=100;
	// ---step1 downloadImage
	String[] tokens=new String[1000];

	private static Lock lock= new ReentrantLock();


	public  String gettoken(String url) throws IOException {
		final HttpClient httpClient = HttpClientBuilder.create().build();

		final HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36");
		// 请求http
		final HttpResponse response = httpClient.execute(httpGet);
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			System.err.println("Method failed: " + response.getStatusLine());
		}
		String stri = EntityUtils.toString(response.getEntity());
		// save img
		String token =stri.split("=")[1].split("\"")[0];
		return token;
	}

	public String getValidCode(String url) throws IOException {
		final HttpClient httpClient = HttpClientBuilder.create().build();

		final HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36");
		// 请求http
		final HttpResponse response = httpClient.execute(httpGet);
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			System.err.println("Method failed: " + response.getStatusLine());
		}
		String stri = EntityUtils.toString(response.getEntity());
		// save img
		String data =stri.split(",\"data\":")[1].split(",\"ms")[0];
		return data;
	}

	public String getinfor(String url) throws IOException {
		final HttpClient httpClient = HttpClientBuilder.create().build();

		final HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36");
		// 请求http
		final HttpResponse response = httpClient.execute(httpGet);
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			System.err.println("Method failed: " + response.getStatusLine());
		}
		String stri = EntityUtils.toString(response.getEntity());
		// save img
		String name =stri.split("userName\":\"")[1].split("\",\"userType")[0];
		return name;
	}

	public void SignIn(String url) throws IOException {
		final HttpClient httpClient = HttpClientBuilder.create().build();

		final HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36");
		// 请求http
		final HttpResponse response = httpClient.execute(httpGet);
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			System.err.println("Method failed: " + response.getStatusLine());
		}

	}

	public String GetCaptcha(String url) throws IOException {
		final HttpClient httpClient = HttpClientBuilder.create().build();

		final HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36");
		// 请求http
		final HttpResponse response = httpClient.execute(httpGet);
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			System.err.println("Method failed: " + response.getStatusLine());
		}
		String stri = EntityUtils.toString(response.getEntity());
		// save img
		String Captcha =stri.split("e\":\"200\",\"")[1].split("\",\"msg\":\"\"}")[0];
		return Captcha;
	}


	public int isWhite(int colorInt, int whiteThreshold) {
		final Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() > whiteThreshold) {
			return 1;
		}
		return 0;
	}

	public int isBlack(int colorInt, int whiteThreshold) {
		final Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() <= whiteThreshold) {
			return 1;
		}
		return 0;
	}

	public BufferedImage removeBlank(BufferedImage img, int whiteThreshold, int white) throws Exception {
		final int width = img.getWidth();
		final int height = img.getHeight();
		int start = 0;
		int end = 0;
		Label1: for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				if (isWhite(img.getRGB(x, y), whiteThreshold) == white) {
					start = y;
					break Label1;
				}
			}
		}
		Label2: for (int y = height - 1; y >= 0; --y) {
			for (int x = 0; x < width; ++x) {
				if (isWhite(img.getRGB(x, y), whiteThreshold) == white) {
					end = y;
					break Label2;
				}
			}
		}
		return img.getSubimage(0, start, width, end - start + 1);
	}

	public BufferedImage removeBackgroud(String picFile, int whiteThreshold) throws Exception {
		final BufferedImage img = ImageIO.read(new File(picFile));
		final int width = img.getWidth();
		final int height = img.getHeight();
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (isWhite(img.getRGB(x, y), whiteThreshold) == 1) {
					img.setRGB(x, y, Color.WHITE.getRGB());
				} else {
					img.setRGB(x, y, Color.BLACK.getRGB());
				}
			}
		}
		return img;
	}



	public BufferedImage removeBackgroudFormBi(BufferedImage img, int whiteThreshold) throws Exception {
		final int width = img.getWidth();
		final int height = img.getHeight();
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (isWhite(img.getRGB(x, y), whiteThreshold) == 1) {
					img.setRGB(x, y, Color.WHITE.getRGB());
				} else {
					img.setRGB(x, y, Color.BLACK.getRGB());
				}
			}
		}
		return img;
	}

	public BufferedImage removepx(BufferedImage img, int px) throws Exception {

		final int width = img.getWidth();
		final int height = img.getHeight();
		for (int x = 1; x < width-1; ++x) {
			for (int y = 1; y < height-1; ++y) {
				int count = 0;
				if (img.getRGB(x-1, y-1) == Color.BLACK.getRGB()) {
					count++;
				}
				if (img.getRGB(x-1, y) == Color.BLACK.getRGB()) {
					count++;
				}
				if (img.getRGB(x-1, y+1) == Color.BLACK.getRGB()) {
					count++;
				}
				if (img.getRGB(x, y-1) == Color.BLACK.getRGB()) {
					count++;
				}
				if (img.getRGB(x, y+1) == Color.BLACK.getRGB()) {
					count++;
				}
				if (img.getRGB(x+1, y-1) == Color.BLACK.getRGB()) {
					count++;
				}
				if (img.getRGB(x+1, y) == Color.BLACK.getRGB()) {
					count++;
				}
				if (img.getRGB(x+1, y+1) == Color.BLACK.getRGB()) {
					count++;
				}

				if(count<=px) {
					img.setRGB(x, y, Color.WHITE.getRGB());
				}
			}
		}
		return img;
	}

	public Map<BufferedImage, String> loadTrainData(String category) throws Exception {
		scaleTraindata(category,540);
		final Map<BufferedImage, String> map = new HashMap<BufferedImage, String>();
		final File dir = new File("train/" + category);
		final File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jpg");
			}
		});
		for (final File file : files) {
			map.put(ImageIO.read(file), file.getName().charAt(0) + "");
		}
		return map;
	}

	public void scaleTraindata(String category, int threshold) throws Exception {
		final File dir = new File("train/" + category);
		final File dataFile = new File("train/" + category + "/data.txt");
		final FileOutputStream fs = new FileOutputStream(dataFile);
		final File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jpg");
			}
		});
		for (final File file : files) {
			final BufferedImage img = ImageIO.read(file);
			final ScaleFilter sf = new ScaleFilter(60, 19);
			BufferedImage imgdest = new BufferedImage(60, 19, img.getType());
			imgdest = sf.filter(img, imgdest);
			new File("train/svm/" + category).mkdirs();
			ImageIO.write(imgdest, "JPG", new File("train/svm/" + category + "/" + file.getName()));
			fs.write((file.getName().charAt(0) + " ").getBytes());
			int index = 1;
			for (int x = 0; x < imgdest.getWidth(); ++x) {
				for (int y = 0; y < imgdest.getHeight(); ++y) {
					fs.write((index++ + ":" + isBlack(imgdest.getRGB(x, y), threshold) + " ").getBytes());
				}
			}
			fs.write("\r\n".getBytes());
		}
		fs.close();

	}

	public void imgToSvmInput(BufferedImage img, String dataFile, int threshold) throws Exception {
		final FileOutputStream fs = new FileOutputStream(dataFile);
		final ScaleFilter sf = new ScaleFilter(60, 19);
		BufferedImage imgdest = new BufferedImage(60, 19, img.getType());
		imgdest = sf.filter(img, imgdest);
		fs.write(("0 ").getBytes());
		int index = 1;
		for (int x = 0; x < imgdest.getWidth(); ++x) {
			for (int y = 0; y < imgdest.getHeight(); ++y) {
				fs.write((index++ + ":" + isBlack(imgdest.getRGB(x, y), threshold) + " ").getBytes());
			}
		}
		fs.write("\r\n".getBytes());
		fs.close();
	}


	private double atof(String s) {
		return Double.valueOf(s).doubleValue();
	}

	private synchronized int atoi(String s) {
		return Integer.parseInt(s);
	}

	private synchronized void predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability)
			throws IOException {
		int correct = 0;
		int total = 0;
		double error = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

		final int svm_type = svm.svm_get_svm_type(model);
		final int nr_class = svm.svm_get_nr_class(model);
		double[] prob_estimates = null;

		if (predict_probability == 1) {
			if (svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
				System.out
						.print("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="
								+ svm.svm_get_svr_probability(model) + "\n");
			} else {
				final int[] labels = new int[nr_class];
				svm.svm_get_labels(model, labels);
				prob_estimates = new double[nr_class];
				output.writeBytes("labels");
				for (int j = 0; j < nr_class; j++) {
					output.writeBytes(" " + labels[j]);
				}
				output.writeBytes("\n");
			}
		}
		while (true) {
			final String line = input.readLine();
			if (line == null) {
				break;
			}

			final StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

			final double target = atof(st.nextToken());
			final int m = st.countTokens() / 2;
			final svm_node[] x = new svm_node[m];
			for (int j = 0; j < m; j++) {
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}

			double v;
			if (predict_probability == 1 && (svm_type == svm_parameter.C_SVC || svm_type == svm_parameter.NU_SVC)) {
				v = svm.svm_predict_probability(model, x, prob_estimates);
				output.writeBytes(v + " ");
				for (int j = 0; j < nr_class; j++) {
					output.writeBytes(prob_estimates[j] + " ");
				}
				output.writeBytes("\n");
			} else {
				v = svm.svm_predict(model, x);
				output.writeBytes(v + "\n");
			}

			if (v == target) {
				++correct;
			}
			error += (v - target) * (v - target);
			sumv += v;
			sumy += target;
			sumvv += v * v;
			sumyy += target * target;
			sumvy += v * target;
			++total;
		}
		if (svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
			System.out.print("Mean squared error = " + error / total + " (regression)\n");
			System.out
					.print("Squared correlation coefficient = "
							+ ((total * sumvy - sumv * sumy) * (total * sumvy - sumv * sumy))
							/ ((total * sumvv - sumv * sumv) * (total * sumyy - sumy * sumy))
							+ " (regression)\n");
		} else {

		}
	}

	private void exit_with_help() {
		System.err.print("usage: svm_predict [options] test_file model_file output_file\n" + "options:\n"
				+ "-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n");
		System.exit(1);
	}

	public synchronized void main1(String argv[]) throws IOException {
		int i, predict_probability = 0;

		// parse options
		for (i = 0; i < argv.length; i++) {
			if (argv[i].charAt(0) != '-') {
				break;
			}
			++i;
			switch (argv[i - 1].charAt(1)) {
				case 'b':
					predict_probability = atoi(argv[i]);
					break;
				default:
					System.err.print("Unknown option: " + argv[i - 1] + "\n");
					exit_with_help();
			}
		}
		if (i >= argv.length - 2) {
			exit_with_help();
		}
		try {
			final BufferedReader input = new BufferedReader(new FileReader(argv[i]));
			final DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(argv[i + 2])));
			final svm_model model = svm.svm_load_model(argv[i + 1]);
			if (predict_probability == 1) {
				if (svm.svm_check_probability_model(model) == 0) {
					System.err.print("Model does not support probabiliy estimates\n");
					System.exit(1);
				}
			} else {
				if (svm.svm_check_probability_model(model) != 0) {
					System.out.print("Model supports probability estimates, but disabled in prediction.\n");
				}
			}
			predict(input, output, model, predict_probability);
			input.close();
			output.close();
		} catch (final FileNotFoundException e) {
			exit_with_help();
		} catch (final ArrayIndexOutOfBoundsException e) {
			exit_with_help();
		}
	}
















	public static void getTokensToTxt(String id,String password) throws IOException {

		File file = new File("tokens.txt");  //存放数组数据的文件
		FileWriter out = new FileWriter(file);  //文件写入流
		String url = "https://api.alphanut.cn/HDU/CasLogin?user="+id+"&pass="+password+"&service=https%3A%2F%2Fskl.hdu.edu.cn%2Fapi%2Fcas%2Flogin%3Findex%3D";
		// 获取token
		CommonUtil CommonUtil=new CommonUtil();
		//将数组中的数据写入到文件中。每行各数据之间TAB间隔
  		for(int i=0;i<ThreadNum;i++){
			String token= null;
			try {
			  token = CommonUtil.gettoken(url);
			} catch (IOException e) {
			  e.printStackTrace();
			}
			System.out.println(token);
			out.write(token+"\r\n");
			}
			out.close();


		}



	public  String[] getTokensFormTxt() throws IOException {

		File file = new File("tokens.txt");  //存放数组数据的文件
		BufferedReader in = new BufferedReader(new FileReader(file));  //
		String line;  //一行数据
		//String[] tokens = new String[1000];
		int row=0;
		//逐行读取，并将每个数组放入到数组中
		while((line = in.readLine()) != null){
			String token = line;

			tokens=line.split("\t");


			row++;
		}
		in.close();
		return tokens;

	}




	public String getAllOcr(String file) throws Exception {
		CommonUtil CommonUtil=new CommonUtil();
		BufferedImage img = removeBackgroud(file, whiteThreshold);
		img=removepx(img,3);
		final List<BufferedImage> listImg = splitImage(img);
		final Map<BufferedImage, String> map = loadTrainData(clazz);
		String result = useSvm ? "" : "";
		for (final BufferedImage bi : listImg) {
			result += getSingleCharOcr(bi, map);
		}
		ImageIO.write(img, "JPG", new File("result/" + clazz + "/" + result + ".jpg"));
		return result;
	}

	public synchronized String getOcrFormBi(BufferedImage img) throws Exception {


		CommonUtil CommonUtil=new CommonUtil();
		img = removeBackgroudFormBi(img, whiteThreshold);
		img=removepx(img,3);
		final List<BufferedImage> listImg = splitImage(img);
		final Map<BufferedImage, String> map = loadTrainData(clazz);
		String result = useSvm ? "" : "";
		for (final BufferedImage bi : listImg) {
			result += getSingleCharOcr(bi, map);
		}
		ImageIO.write(img, "JPG", new File("result/" + clazz + "/" + result + ".jpg"));
		return result;
	}


	private synchronized String getSingleCharOcr(BufferedImage img, Map<BufferedImage, String> map) throws Exception {
		if (useSvm) {
			svm_predict svm_predict=new svm_predict();
			CommonUtil CommonUtil=new CommonUtil();
			final String input = new File("img/" + clazz + "/input.txt").getAbsolutePath();
			final String output = new File("result/" + clazz + "/output.txt").getAbsolutePath();
			imgToSvmInput(img, input, whiteThreshold);
			main1(
					new String[] { input, new File("train/" + clazz + "/data.txt.model").getAbsolutePath(), output });
			final List<String> predict = IOUtils.readLines(new FileInputStream(output));
			if (predict.size() > 0 && predict.get(0).length() > 0) {
				return predict.get(0).substring(0, 1);
			}
			return "#";
		}
		String result = "";
		final int width = img.getWidth();
		final int height = img.getHeight();
		int min = width * height;
		for (final BufferedImage bi : map.keySet()) {
			int count = 0;
			Label1: for (int x = 0; x < width; ++x) {
				for (int y = 0; y < height; ++y) {
					if (img.getRGB(x, y) != bi.getRGB(x, y)) {
						count++;
						if (count >= min) {
							break Label1;
						}
					}
				}
			}
			if (count < min) {
				min = count;
				result = map.get(bi);
			}
		}
		return result;
	}

	private List<BufferedImage> splitImage(BufferedImage img) throws Exception {
		final List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
		subImgs.add(img.getSubimage(1, 2, 15, 19));
		subImgs.add(img.getSubimage(18, 2, 15, 19));
		subImgs.add(img.getSubimage(36, 2, 15, 19));
		subImgs.add(img.getSubimage(53, 2, 15, 19));

		return subImgs;
	}


		public Ocr1(String name){
//重写构造，可以对线程添加名字
			super(name);
		}
		@Override
		public void run() {

				int ThNum=parseInt(this.getName());
				CommonUtil CommonUtil=new CommonUtil();
				try {
					this.getTokensFormTxt();
				} catch (IOException e) {

				}
				try {


					String name=getinfor("https://api.alphanut.cn/Skl/GetUserInfo?token="+tokens[0]);
				} catch (IOException e) {
					e.printStackTrace();
				}

					for(int code=(10000/ThreadNum)*ThNum;code<(10000/ThreadNum)*(ThNum+1);code++){
						long startTime =fromDateStringToLong(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
						System.out.println("code="+code);
						if(code<10) {
							try {
								SignIn("https://api.alphanut.cn/Skl/SignIn?token=" + tokens[ThNum] + "&code=000" + code);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						else if(code<100){
							try {
								SignIn("https://api.alphanut.cn/Skl/SignIn?token=" + tokens[ThNum] + "&code=00" + code);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						else if(code<1000){
							try {
								SignIn("https://api.alphanut.cn/Skl/SignIn?token=" + tokens[ThNum] + "&code=0" + code);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						String base64str= null;
						try {
							base64str = GetCaptcha("https://api.alphanut.cn/Skl/GetCaptcha?token="+tokens[ThNum]).replace("\\","").split(",")[1];
						} catch (IOException e) {
							e.printStackTrace();
						}

						BASE64Decoder decoder = new sun.misc.BASE64Decoder();

						try {

							byte[] bytes1 = decoder.decodeBuffer(base64str);
							ByteArrayInputStream bais = new ByteArrayInputStream(bytes1);
							BufferedImage bi1 = ImageIO.read(bais);

							synchronized (this) {

							String result="";
								try {
									lock.lock();
									result = getOcrFormBi(bi1);
									System.out.println("result=" + result);
								}finally {
									lock.unlock();
								}
								String signreult=getValidCode("https://api.alphanut.cn/Skl/ValidCode?token="+tokens[ThNum]+"&code="+result);
								long stopTime = fromDateStringToLong(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));

//计算时间差,单位毫秒
								long timeSpan = stopTime - startTime;
								System.out.println("time="+timeSpan);

								if(signreult=="200"){
									System.out.println("success");
								}else if(signreult=="401"){

								}

							}





						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}






	public static void main(String[] args) throws Exception {


		//getTokensToTxt("18051923","lipeiyuan12138");
		//getTokensFormTxt();
		for (int i=0;i<ThreadNum;i++){
			String ThNum=Integer.toString(i);
			Ocr1 threadRuning = new Ocr1(ThNum);
			threadRuning.start();
		}

		//new File("img/" + clazz).mkdirs();
		//new File("train/" + clazz).mkdirs();
		//new File("result/" + clazz).mkdirs();
		// 先删除result/ocr目录，开始识别

		// scaleTraindata(clazz, whiteThreshold);
		// svm_train train = new svm_train();
		// train.run(new String[] { new File("train/" + clazz +
		// "/data.txt").getAbsolutePath(),
		// new File("train/" + clazz + "/data.txt.model").getAbsolutePath() });
	}

	/**
	 * 根据String型时间，获取long型时间，单位毫秒
	 * @param inVal 时间字符串
	 * @return long型时间
	 */
	public long fromDateStringToLong(String inVal) {
		Date date = null;
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
		try {
			date = inputFormat.parse(inVal);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date.getTime();
	}
}
