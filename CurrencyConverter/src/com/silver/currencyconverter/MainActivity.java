package com.silver.currencyconverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private EditText mFromCurrency;
	private EditText mToCurrency;
	private Button mbtnConvert;
	private Button mbtnCurrencyCode;
	private String fromCurrency;
	private String toCurrency;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		setContentView(R.layout.activity_main);
		initializeLayout();
		buttonActions();
	}

	private void initializeLayout() {
		mFromCurrency = (EditText) findViewById(R.id.fromConvertEditText);
		mToCurrency = (EditText) findViewById(R.id.toConvertEditText);
		mbtnConvert = (Button) findViewById(R.id.btn_convert);
		mbtnCurrencyCode = (Button) findViewById(R.id.btn_currency_code);
	}

	private void buttonActions() {
		mbtnConvert.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if(chkInputs()) {
					callConverterWS();
				}
			}
		});

		mbtnCurrencyCode.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				nextActivity();
			}
		});
	}

	private boolean chkInputs() {
		boolean canProceed = false;
		View focusView = null;
		this.fromCurrency = mFromCurrency.getText().toString();
		this.toCurrency = mToCurrency.getText().toString();

		if(TextUtils.isEmpty(fromCurrency)) {
			mFromCurrency.setError(getString(R.string.err101));
			focusView = mFromCurrency;
			focusView.requestFocus();
		}
		else if (TextUtils.isEmpty(toCurrency)) {
			mToCurrency.setError(getString(R.string.err101));
			focusView = mToCurrency;
			focusView.requestFocus();
		}

		else
			canProceed = true;

		return canProceed;
	}

	private void callConverterWS() {
		try {
			String xml = convertStreamToString(getAssets().open("request.xml"));
			String RequestString = String.format(xml, this.fromCurrency, this.toCurrency);

			//String URL = "http://www.w3schools.com/webservices/tempconvert.asmx";
			String URL = "http://www.webservicex.net/CurrencyConvertor.asmx?WSDL";
			String ResponseInXML = getResponseByXML(URL,  RequestString);
			readXml(ResponseInXML);
			Log.d("ResponseInXML", ResponseInXML);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getResponseByXML(String URL, String request) {
		HttpPost httpPost = new HttpPost(URL);
		StringEntity entity;
		String response_string = null;
		try {
			entity = new StringEntity(request, HTTP.UTF_8);
			httpPost.setHeader("Content-Type","text/xml;charset=UTF-8");
			httpPost.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(httpPost);
			response_string = EntityUtils.toString(response.getEntity());

			Log.d("request", response_string);
			readXml(response_string);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response_string;
	}

	private void readXml(String responseXml) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf =
				DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(responseXml));

		Document doc = db.parse(is);
		NodeList nodes = doc.getElementsByTagName("ConversionRateResponse");

		for (int i = 0; i < nodes.getLength(); i++) {
			Element element = (Element) nodes.item(i);

			NodeList parentElementList = element.getElementsByTagName("ConversionRateResult");
			Element parentElement = (Element) parentElementList.item(0);
			String parentElementRE = getCharacterDataFromElement(parentElement);
			makeToast("Conversion Rate is: " + parentElementRE);
		}
	}

		public static String getCharacterDataFromElement(Element e) {
			if(e != null) {
				Node child = e.getFirstChild();
				if (child instanceof CharacterData) {
					CharacterData cd = (CharacterData) child;
					return cd.getData();
				}
			}  
			return ""; 
		}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.main, menu);
			return true;
		}

		private String convertStreamToString(InputStream is) throws Exception {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line+"\n");
			}
			is.close();
			return sb.toString();
		}

		private void nextActivity() {
			currencyCodeActivity();
		}
		private void currencyCodeActivity() {
			Intent intent = new Intent(this, CurrencyCodeActivity.class);
			startActivity(intent);
		}
		
		private void makeToast(String errMsg) {
			Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_LONG)
			.show();
		}
	}
