package com.zhuoyou.plugin.bluetooth.product;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

import com.zhuoyou.plugin.running.RunningApp;


public class ProductManager {

	public static ProductManager mProductManager = null;
	private Context mCtx = RunningApp.getInstance().getApplicationContext();
	private Grandpa mLocalGrandPa = null;

	public static ProductManager getInstance(){
		if(mProductManager == null){
			mProductManager = new ProductManager();
		}
		return mProductManager;
	}

	public ProductManager(){
		init();
	}

	private void init(){
		LoadingLocalProduct();
	}
	
	private void LoadingLocalProduct(){
		InputStream is = null;
		
		try {
			is = mCtx.getAssets().open( "product" + ".xml");
			Log.i("gchk", "open local product file successed " );
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(is == null){
			Log.e("gchk", "open local product file failed , can not happen!" );
			return ;
		}
		
		mLocalGrandPa = parserXml(is);
		
		if(mLocalGrandPa == null){
			Log.e("gchk", "parser local xml failed , can not happen!" );
			return ;
		}
	}

	private Grandpa parserXml(InputStream is){
		ProductHandler handler = new ProductHandler();
		Grandpa pa = null;
		try {
			SAXParser parser = null;
			SAXParserFactory factory = SAXParserFactory.newInstance(); 
			parser = factory.newSAXParser();
			parser.parse(is, handler);
			is.close();
			pa = handler.getRoot();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pa;
	}

	public Grandpa getCurrRoot(){
		//@check mofify time and get the seleted root
		return mLocalGrandPa;
	}

	public boolean isSupportPlugin(String product_name , String package_name ){
		Grandpa root = getCurrRoot();
		if(root == null){
			return false;
		}
		
		for(int i = 0 ; i < root.getFather().size() ; i++){
			Father father = root.getFather().get(i);
			if(product_name.equals(father.getName())){
				for(int j = 0 ; j < father.getSons().size() ; j++){
					Son son = father.getSons().get(j);
					if(son.getPackageName().equals(package_name)){
						return true;
					}
				}
			}
		}
		
		return false;
	}

	/**
	 * get product category from its name
	 * @param product_name
	 * @return
	 */
	public String getProductCategory(String product_name){
		Grandpa root = getCurrRoot();
		
		if(root == null){
			return null;
		}
		
		for(int i = 0 ; i < root.getFather().size() ; i++){
			Father father = root.getFather().get(i);
			if(product_name.equals(father.getName())){
				return father.getCategory();
			}
		}
		
		return null;
	}

}
