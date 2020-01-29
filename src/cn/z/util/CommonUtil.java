package cn.z.util;

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


public class CommonUtil  {

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

}
