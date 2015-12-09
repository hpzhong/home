package com.zhuoyou.plugin.bluetooth.attach;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.zhuoyou.plugin.bluetooth.product.ProductManager;
import com.zhuoyou.plugin.running.R;
import com.zhuoyou.plugin.running.RunningApp;

public class PluginManager {
	
	private static final String PLUG_PACKAGENAME_PREX = "com.zhuoyou.plugin.";
	private static PluginManager mPluginManager = null;
	private Context mCtx = RunningApp.getInstance().getApplicationContext();
	private PackageManager mPackageManager = mCtx.getPackageManager();
	private List<PlugBean> mPlugBeans;
	private List<PlugBean> mSystemPlugBeans = null;
	private List<PreInstallBean> mPreInstallBeans = null;
	private List<String> mInstalledPlugs = null;
	public boolean hasNotication;

	public static PluginManager getInstance() {
		if (mPluginManager == null) {
			mPluginManager = new PluginManager();
			return mPluginManager;
		}
		return mPluginManager;
	}

	public PluginManager() {
		getInstalledPlugs();
		createSystemPlug();
		loadPreInstallPlug();
	}

	public void getInstalledPlugs() {
		if (mInstalledPlugs != null && mInstalledPlugs.size() > 0) {
			mInstalledPlugs.clear();
			mInstalledPlugs = null;
		}
		mInstalledPlugs = new ArrayList<String>();
		List<PackageInfo> pkgs = mPackageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (PackageInfo pkg : pkgs) {
			if (pkg.packageName.startsWith(PLUG_PACKAGENAME_PREX) && !pkg.packageName.equals(mCtx.getPackageName())) {
				mInstalledPlugs.add(pkg.packageName);
			} else {
				continue;
			}
		}
	}
	
	private void createSystemPlug() {
		if (mSystemPlugBeans != null && mSystemPlugBeans.size() > 0) {
			mSystemPlugBeans.clear();
			mSystemPlugBeans = null;
		}
		mSystemPlugBeans = new ArrayList<PlugBean>();

		PlugBean bean = new PlugBean();
		bean.isInstalled = false;
		bean.isPreInstall = true;
		bean.isSystem = true;
		bean.mLogoResId = R.drawable.plug_system_notify;
		bean.mTitle = mCtx.getString(R.string.notification_preference_title);
		bean.mMethod_Entry = "com.tyd.bt.NoticationActivity";
		mSystemPlugBeans.add(bean);

		bean = new PlugBean();
		bean.isInstalled = false;
		bean.isPreInstall = true;
		bean.isSystem = true;
		bean.mLogoResId = R.drawable.plug_system_call;
		bean.mTitle = mCtx.getString(R.string.phone_preference_category);
		bean.mMethod_Entry = "com.tyd.bt.CallServiceActivity";
		mSystemPlugBeans.add(bean);

		bean = new PlugBean();
		bean.isInstalled = false;
		bean.isPreInstall = true;
		bean.isSystem = true;
		bean.mLogoResId = R.drawable.plug_system_msg;
		bean.mTitle = mCtx.getString(R.string.sms_preference_title);
		bean.mMethod_Entry = "com.tyd.bt.SmsServiceActivity";
		mSystemPlugBeans.add(bean);
	}
	
	private void loadPreInstallPlug() {
		InputStream is = null;
		String loc = Locale.getDefault().getCountry();

		try {
			is = mCtx.getAssets().open("preinstall/xml/preinstall_" + loc + ".xml");
			Log.i("gchk", "open preinstall file successed ,file =" + "preinstall/xml/preinstall_" + loc + ".xml");
		} catch (IOException e) {
			Log.i("gchk", "open preinstall file failed ,file =" + "preinstall/xml/preinstall_" + loc + ".xml");
			e.printStackTrace();
		}
		if (is == null) {
			try {
				is = mCtx.getAssets().open("preinstall/xml/preinstall_" + "US" + ".xml");
				Log.i("gchk", "open assert profile LAN = US force");
			} catch (IOException e) {
				Log.e("gchk", "open assert profile LAN = US force ERROR " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		if (is == null) {
			Log.e("gchk", "didn't find en profile , can not happen!");
			return;
		}

		if (mPreInstallBeans != null && mPreInstallBeans.size() > 0) {
			mPreInstallBeans.clear();
			mPreInstallBeans = null;
		}

		PreInstallHandler handler = new PreInstallHandler();
		try {
			SAXParser parser = null;
			SAXParserFactory factory = SAXParserFactory.newInstance();
			parser = factory.newSAXParser();
			parser.parse(is, handler);
			is.close();
			mPreInstallBeans = handler.getRoot();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (mPreInstallBeans == null || mPreInstallBeans.size() == 0) {
			Log.e("gchk", "parser preinstall xml failed or current product didn't contain preinstall information , can not happen!");
			return;
		}
	}
	
	public void processPlugList(String curr_nickname) {
		if (mPlugBeans != null && mPlugBeans.size() > 0) {
			mPlugBeans.clear();
			mPlugBeans = null;
		}
		mPlugBeans = new ArrayList<PlugBean>();
		//add system plug to list
		hasNotication = false;
		for (int k = 0; k < mSystemPlugBeans.size(); k++) {
			if (ProductManager.getInstance().isSupportPlugin(curr_nickname, mSystemPlugBeans.get(k).mMethod_Entry)) {
				mPlugBeans.add(mSystemPlugBeans.get(k));
				if (k == 0)
					hasNotication = true;
			}
		}
		//add preinstall plugin to list
		if (mPreInstallBeans != null && mPreInstallBeans.size() > 0) {
			for (int i = 0; i < mPreInstallBeans.size(); i++) {
				PreInstallBean preinstallbean = mPreInstallBeans.get(i);
				String productName = preinstallbean.getName();
				if (productName.equals(curr_nickname)) {
					List<String> packagenames = preinstallbean.getPlugPackageNames();
					List<String> names = preinstallbean.getPlugNames();
					if (packagenames != null && packagenames.size() > 0) {
						for (int j = 0; j < packagenames.size(); j++) {
							String j_packagename = packagenames.get(j);
							String j_name = names.get(j);
							PlugBean plugbean = new PlugBean();
							if (mInstalledPlugs.contains(j_packagename)) {
								plugbean.isInstalled = true;
							} else {
								plugbean.isInstalled = false;
							}
							plugbean.isPreInstall = true;
							plugbean.isSystem = false;
							plugbean.mPackageName = j_packagename;
							plugbean.mTitle = j_name;
							String imageName = j_packagename.replaceAll("\\.", "_");
							Resources resource = mCtx.getResources();
							int resID = resource.getIdentifier(imageName, "drawable", mCtx.getPackageName());
							if (resID == 0) {
								resID = R.drawable.com_zhuoyou_plugin_antilost;
							}
							plugbean.mLogoBitmap = resource.getDrawable(resID);
							mPlugBeans.add(plugbean);
						}
					}
				}
			}
		}
		//add other installed plugin to list
		if (mInstalledPlugs != null && mInstalledPlugs.size() > 0) {
			List<PlugBean> temps = new ArrayList<PlugBean>();
			for (int i = 0; i < mInstalledPlugs.size(); i++) {
				String install_packageName = mInstalledPlugs.get(i);
				boolean need_add = true;
				for (PlugBean bean : mPlugBeans) {
					if (bean.mPackageName.equals(install_packageName)) {
						need_add = false;
						break;
					}
				}
				if (need_add) {
					PlugBean plugin = new PlugBean();
					plugin.mPackageName = install_packageName;
					plugin.isInstalled = true;
					plugin.isSystem = false;
					plugin.isPreInstall = false;
					temps.add(plugin);
				}
			}
			mPlugBeans.addAll(mPlugBeans.size(), temps);
		}
		//final check list according to nickname
		List<PlugBean> temps = new ArrayList<PlugBean>();
		for (int i = 0; i < mPlugBeans.size(); i++) {
			PlugBean bean = mPlugBeans.get(i);
			if (!bean.isSystem) {
				if (ProductManager.getInstance().isSupportPlugin(curr_nickname, bean.mPackageName)) {
					temps.add(bean);
				}
			} else {
				temps.add(bean);
			}
		}

		if (mPlugBeans != null && mPlugBeans.size() > 0) {
			mPlugBeans.clear();
			mPlugBeans = null;
		}
		mPlugBeans = new ArrayList<PlugBean>();
		mPlugBeans.addAll(temps);

		for (int i = 0; i < mPlugBeans.size(); i++) {
			PlugBean bean = mPlugBeans.get(i);
			if (bean.isInstalled) {
				registerPlug(bean);
			}
		}
	}
	
	private void registerPlug(PlugBean bean) {
		Context targetContext = null;
		try {
			targetContext = mCtx.createPackageContext(bean.mPackageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
			bean.mCtx = targetContext;
			Log.e("gchk", bean.mPackageName + " CONTEXT = " + bean.mCtx.toString());
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Log.i("gchk", "NameNotFoundException" + e.getMessage());
			return;
		}

		Class<?> c = null;
		try {
			c = targetContext.getClassLoader().loadClass(bean.mPackageName + "." + "PlugMain");
			bean.mClasses = c;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Log.i("gchk", "ClassNotFoundException" + e.getMessage());
			return;
		}

		Constructor<?> constructor = null;
		try {
			constructor = c.getConstructor(Context.class, Context.class);
			bean.mConstructor = constructor;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			Log.i("gchk", "NoSuchMethodException" + e.getMessage());
			return;
		}

		Object plug = null;
		try {
			plug = constructor.newInstance(targetContext, mCtx);
			bean.mInstance = plug;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Log.i("gchk", "IllegalArgumentException" + e.getMessage());
			return;
		} catch (InstantiationException e) {
			e.printStackTrace();
			Log.i("gchk", "InstantiationException" + e.getMessage());
			return;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			Log.i("gchk", "IllegalAccessException" + e.getMessage());
			return;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			Log.i("gchk", "InvocationTargetException" + e.getMessage());
			return;
		}

		/**
		 * start register
		 */
		Object obj = null;
		obj = PlugUtils.invoke(bean, "getIcon");
		if (obj != null) {
			bean.mLogoBitmap = (Drawable) obj;
		}

		obj = PlugUtils.invoke(bean, "getName");
		if (obj != null) {
			bean.mTitle = (String) obj;
		}

		obj = PlugUtils.invoke(bean, "getSupportCmd");
		if (obj != null) {
			bean.mSupportCmd = (String) obj;
		}

		obj = PlugUtils.invoke(bean, "getEntryMethodName");
		if (obj != null) {
			bean.mMethod_Entry = (String) obj;
		}

		obj = PlugUtils.invoke(bean, "getWorkMethodName");
		if (obj != null) {
			bean.mWorkMethod = (Map<String, String>) obj;
		}
	}
	
	public void updatePlugList(String packagename, boolean add) {
		if (!packagename.startsWith(PLUG_PACKAGENAME_PREX)) {
			Log.i("gchk", "not plugin , don't care~haha");
			return;
		}

		getInstalledPlugs();
		if (mPlugBeans != null && mPlugBeans.size() > 0) {
			if (add == false) {
				for (int i = 0; i < mPlugBeans.size(); i++) {
					PlugBean bean = mPlugBeans.get(i);
					if (bean.mPackageName.equals(packagename)) {
						if (!bean.isPreInstall) {
							mPlugBeans.remove(i);
						} else {
							bean.isInstalled = false;
							String imageName = bean.mPackageName.replaceAll("\\.", "_");
							Resources resource = mCtx.getResources();
							int resID = resource.getIdentifier(imageName, "drawable", mCtx.getPackageName());
							if (resID == 0) {
								resID = R.drawable.com_zhuoyou_plugin_antilost;
							}
							bean.mLogoBitmap = resource.getDrawable(resID);
						}
						return;
					}
				}
			} else {
				boolean find = false;
				for (int i = 0; i < mPlugBeans.size(); i++) {
					PlugBean bean = mPlugBeans.get(i);
					if (bean.mPackageName.equals(packagename)) {
						find = true;
						bean.isInstalled = true;
						registerPlug(bean);
						return;
					}
				}

				if (!find) {
					PlugBean t = new PlugBean();
					t.isPreInstall = false;
					t.isInstalled = true;
					t.isSystem = false;
					t.mPackageName = packagename;
					registerPlug(t);
					// insert to last two postion， the last one must be add
					mPlugBeans.add(mPlugBeans.size() - 1, t);
				}
			}
		}
	}
	
	public List<PlugBean> getPlugBeans() {
		return mPlugBeans;
	}

	public static void release() {
		mPluginManager = null;
	}

}
