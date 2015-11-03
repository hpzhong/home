package com.zhuoyou.plugin.bluetooth.attach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyou.plugin.bluetooth.data.IgnoreList;
import com.zhuoyou.plugin.bluetooth.data.Util;
import com.zhuoyou.plugin.running.R;

public class SelectNotifiActivity extends Activity {
    // Tab tag enum
    private static final String TAB_TAG_PERSONAL_APP = "personal_app";
    private static final String TAB_TAG_SYSTEM_APP = "system_app";
    
    // View item filed
    private static final String VIEW_ITEM_ICON = "package_icon";
    private static final String VIEW_ITEM_TEXT = "package_text";
    private static final String VIEW_ITEM_CHECKBOX = "package_checkbox";
    private static final String VIEW_ITEM_NAME = "package_name";    // Only for save to ignore list
    
    // These two array should be consistent 
    private static final String[] VIEW_TEXT_ARRAY = new String[] { VIEW_ITEM_ICON, VIEW_ITEM_TEXT, VIEW_ITEM_CHECKBOX };
    private static final int[] VIEW_RES_ID_ARRAY = new int[] { R.id.package_icon, R.id.package_text, R.id.package_switch };
    
    // For save tab widget
    private TabHost mTabHost = null;
    private View mView1, mView2;
   
    // For personal app list
    private List<Map<String, Object>> mPersonalAppList = null;
    private SimpleAdapter mPersonalAppAdapter = null;
    
    // For system app list
    private List<Map<String, Object>> mSystemAppList = null;
    private SimpleAdapter mSystemAppAdapter = null;

    // For select all button
    private int mPersonalAppSelectedCount = 0;
    private int mSystemAppSelectedCount = 0;
    private Button mSelectAllPersonalAppButton = null;
    private Button mSelectAllSystemAppButton = null;


    public SelectNotifiActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_notifi_activity_layout);                
		TextView tv_title = (TextView) findViewById(R.id.title);
		tv_title.setText(R.string.select_notifi_activity);
		RelativeLayout im_back = (RelativeLayout) findViewById(R.id.back);
		im_back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
        initTabHost();                        
        LoadPackageTask loadPackageTask = new LoadPackageTask(this);
        loadPackageTask.execute("");
    }

    private View getTabIndicatorView(Context context, int textViewId)
	{
		View tabIndicatorView = LayoutInflater.from(context).inflate(R.layout.tab_indicator, null);
		TextView mTextView = (TextView) tabIndicatorView.findViewById(R.id.txt_indicator);
		mTextView.setText(textViewId);
		return tabIndicatorView;
	}

    private void initTabHost() {
    	mView1 = (View) findViewById(R.id.view1);
    	mView2 = (View) findViewById(R.id.view2);
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();      
        mTabHost.addTab(mTabHost.newTabSpec(TAB_TAG_PERSONAL_APP).setContent(
                R.id.LinearLayout001).setIndicator(getTabIndicatorView(this, R.string.personal_apps_title)));
        mTabHost.addTab(mTabHost.newTabSpec(TAB_TAG_SYSTEM_APP).setContent(
                R.id.LinearLayout002).setIndicator(getTabIndicatorView(this, R.string.system_apps_title)));
        updateTab(mTabHost);
        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				mTabHost.setCurrentTabByTag(tabId);
				updateTab(mTabHost);
				switch(mTabHost.getCurrentTab()) {
				case 0:
					mView1.setVisibility(View.VISIBLE);
					mView2.setVisibility(View.INVISIBLE);
					break;
				case 1:
					mView1.setVisibility(View.INVISIBLE);
					mView2.setVisibility(View.VISIBLE);
					break;
				}
				
			}
        	
        });
    }
    
    private void updateTab(final TabHost tabHost) {
    	for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(R.id.txt_indicator);
			if (tabHost.getCurrentTab() == i) {
				tv.setTextColor(0xFF1283CF);
			}else {
				tv.setTextColor(0xFF687D87);
			}
    	}
    }
    
    private void initUiComponents() {
        OnItemClickListener listener = new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                @SuppressWarnings("unchecked")
                Map<String, Object> item = (Map<String, Object>) arg0.getItemAtPosition(arg2);
                if (item == null) {
                    return;
                }                
                
                // Toggle item selection
                boolean isSelected = (Boolean) item.get(VIEW_ITEM_CHECKBOX);
                item.remove(VIEW_ITEM_CHECKBOX);
                item.put(VIEW_ITEM_CHECKBOX, !isSelected);
                
                // update list data
                int countVariation = (isSelected ? -1 : 1);                
                if (isPersonalAppTabSelected()) {
                    mPersonalAppSelectedCount += countVariation;
                    mPersonalAppAdapter.notifyDataSetChanged();                    
                    updateSelectAllButtonText(TAB_TAG_PERSONAL_APP);
                } else {
                    mSystemAppSelectedCount += countVariation;
                    mSystemAppAdapter.notifyDataSetChanged();
                    updateSelectAllButtonText(TAB_TAG_SYSTEM_APP);
                }
            }
        };
        
        // Initialize personal app list view
        ListView mPersonalAppListView = (ListView) findViewById(R.id.list_personal_app);
        mPersonalAppAdapter = createAdapter(mPersonalAppList);
        mPersonalAppListView.setAdapter(mPersonalAppAdapter);                
        mPersonalAppListView.setOnItemClickListener(listener);
        
        // Initialize system app list view
        ListView mSystemAppListView = (ListView) findViewById(R.id.list_system_app);
        mSystemAppAdapter = createAdapter(mSystemAppList);
        mSystemAppListView.setAdapter(mSystemAppAdapter);
        mSystemAppListView.setOnItemClickListener(listener);

        // Init command buttons
        initCmdBtns();
    }

    private SimpleAdapter createAdapter(List<Map<String, Object>> dataList) {
        SimpleAdapter adapter = new SimpleAdapter(this, dataList, R.layout.package_list_layout,
                VIEW_TEXT_ARRAY, VIEW_RES_ID_ARRAY);
        adapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if ((view instanceof ImageView) && (data instanceof Drawable)) {
                    ImageView iv = (ImageView) view;
                    iv.setImageDrawable((Drawable) data);
                    return true;
                }

                return false;
            }
        });

        return adapter;
    }

    private void saveIgnoreList() {
        HashSet<String> ignoreList = new HashSet<String>();
               
        // Save personal app
        for (Map<String, Object> personalAppItem : mPersonalAppList) {
            boolean isSelected = (Boolean) personalAppItem.get(VIEW_ITEM_CHECKBOX);
            if (isSelected) {
                String appName = (String) personalAppItem.get(VIEW_ITEM_NAME);
                ignoreList.add(appName);
            }
        }
        
        // Save system app
        for (Map<String, Object> systemAppItem : mSystemAppList) {
            boolean isSelected = (Boolean) systemAppItem.get(VIEW_ITEM_CHECKBOX);
            if (isSelected) {
                String appName = (String) systemAppItem.get(VIEW_ITEM_NAME);
                ignoreList.add(appName);
            }
        }

        // Save to file
        IgnoreList.getInstance().saveIgnoreList(ignoreList);
        
        // Prompt user that have saved successfully .
        Toast.makeText(this, R.string.save_successfully, Toast.LENGTH_SHORT).show();
    }

    private boolean isPersonalAppTabSelected() {        
        return (mTabHost.getCurrentTabTag() == TAB_TAG_PERSONAL_APP);
    }
    
    private void initCmdBtns() {
        // Save selected app
        OnClickListener saveButtonListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIgnoreList();
            }
        };
        
        // Init save button
        Button savePersonalAppButton = (Button) findViewById(R.id.button_save_personal_app);
        savePersonalAppButton.setVisibility(View.VISIBLE);
        savePersonalAppButton.setOnClickListener(saveButtonListener);
        Button saveSystemAppButton = (Button) findViewById(R.id.button_save_system_app);
        saveSystemAppButton.setVisibility(View.VISIBLE);
        saveSystemAppButton.setOnClickListener(saveButtonListener);

        // Select/Deselect all list item
        OnClickListener selectButtonListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAllListItemSelection();                
            }
        };
        
        // Init select all button
        mSelectAllPersonalAppButton = (Button) findViewById(R.id.button_select_all_personal_app);
        mSelectAllPersonalAppButton.setVisibility(View.VISIBLE);
        mSelectAllPersonalAppButton.setOnClickListener(selectButtonListener);
        updateSelectAllButtonText(TAB_TAG_PERSONAL_APP);
        mSelectAllSystemAppButton = (Button) findViewById(R.id.button_select_all_system_app);
        mSelectAllSystemAppButton.setVisibility(View.VISIBLE);
        mSelectAllSystemAppButton.setOnClickListener(selectButtonListener);
        updateSelectAllButtonText(TAB_TAG_SYSTEM_APP);
    }

    private void toggleAllListItemSelection() {
        boolean isAllSelected;
        if (isPersonalAppTabSelected()) {
            /*
             * toggle personal app list
             */
            
            isAllSelected = (mPersonalAppSelectedCount == mPersonalAppList.size());
            
            // Check or uncheck all personal app item
            for (Map<String, Object> personalAppItem : mPersonalAppList) {                   
                personalAppItem.remove(VIEW_ITEM_CHECKBOX);
                personalAppItem.put(VIEW_ITEM_CHECKBOX, !isAllSelected);
            }
            
            // Update list data
            mPersonalAppSelectedCount = (isAllSelected ? 0 : mPersonalAppList.size());
            mPersonalAppAdapter.notifyDataSetChanged();
            updateSelectAllButtonText(TAB_TAG_PERSONAL_APP);            
        } else {
            /*
             * toggle system app list
             */
            
            isAllSelected = (mSystemAppSelectedCount == mSystemAppList.size());
            
            // Check or uncheck all system app item
            for (Map<String, Object> systemAppItem : mSystemAppList) {                   
                systemAppItem.remove(VIEW_ITEM_CHECKBOX);
                systemAppItem.put(VIEW_ITEM_CHECKBOX, !isAllSelected);
            }
            
            // Update list data
            mSystemAppSelectedCount = (isAllSelected ? 0 : mSystemAppList.size());
            mSystemAppAdapter.notifyDataSetChanged();
            updateSelectAllButtonText(TAB_TAG_SYSTEM_APP);
        }
    }
    
    private void updateSelectAllButtonText(String tabTag) {
        boolean isAllSelected;
        Button selectAllButton;
        if (tabTag.equals(TAB_TAG_PERSONAL_APP)) {
            isAllSelected = (mPersonalAppSelectedCount == mPersonalAppList.size());
            selectAllButton = mSelectAllPersonalAppButton;
        } else {
            isAllSelected = (mSystemAppSelectedCount == mSystemAppList.size());
            selectAllButton = mSelectAllSystemAppButton;
        }
        
        if (isAllSelected) {
            selectAllButton.setText(R.string.button_deselect_all);
        } else {
            selectAllButton.setText(R.string.button_select_all);
        }
        
    }
    
    /**
     * This class is used for sorting package list.
     */
    private class PackageItemComparator implements Comparator<Map<String, Object>> {

        private final String mKey;

        public PackageItemComparator() {
            mKey = SelectNotifiActivity.VIEW_ITEM_TEXT;
        }


        /**
         * Compare package in alphabetical order.
         * @see java.util.Comparator#compare(Object, Object)
         */
        @Override
        public int compare(Map<String, Object> packageItem1, Map<String, Object> packageItem2) {

            String packageName1 = (String) packageItem1.get(mKey);
            String packageName2 = (String) packageItem2.get(mKey);
            return packageName1.compareToIgnoreCase(packageName2);
        }
    } 
    
    private class LoadPackageTask extends AsyncTask<String, Integer, Boolean> {

        private ProgressDialog mProgressDialog = null;
        private final Context mContext;
        
        public LoadPackageTask(Context context) {
            mContext = context;             
            createProgressDialog();
        }

        /*
         * Show a ProgressDialog to prompt user to wait
         */
        private void createProgressDialog() {
        	if (mProgressDialog == null) {
	            mProgressDialog = new ProgressDialog(mContext);
	            mProgressDialog.setMessage(mContext.getString(R.string.progress_dialog_message));
        	}
            mProgressDialog.show();
        }
                
        @Override
        protected Boolean doInBackground(String... arg0) {
            
            // Load and sort package list
            loadPackageList();
            sortPackageList();
            
            return true;
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            // Do the operation after load and sort package list completed
            initUiComponents();
            
            if (mProgressDialog != null) {
                mProgressDialog.dismiss(); 
                mProgressDialog = null;
            }
        }
        
        private void loadPackageList() {
            mPersonalAppList = new ArrayList<Map<String, Object>>();
            mSystemAppList = new ArrayList<Map<String, Object>>();
            HashSet<String> ignoreList = IgnoreList.getInstance().getIgnoreList();
            HashSet<String> exclusionList = IgnoreList.getInstance().getExclusionList();
            List<PackageInfo> packagelist = getPackageManager().getInstalledPackages(0);

            for (PackageInfo packageInfo : packagelist) {
                if (packageInfo != null) {
                    // Whether this package should be exclude;
                    if (exclusionList.contains(packageInfo.packageName)) {
                        continue;
                    }

                    /*
                     * Add this package to package list
                     */
                    Map<String, Object> packageItem = new HashMap<String, Object>();
                    // Add app icon
                    Drawable icon = mContext.getPackageManager()
                            .getApplicationIcon(packageInfo.applicationInfo);
                    packageItem.put(VIEW_ITEM_ICON, icon);

                    // Add app name
                    String appName = mContext.getPackageManager()
                            .getApplicationLabel(packageInfo.applicationInfo).toString();
                    packageItem.put(VIEW_ITEM_TEXT, appName);
                    packageItem.put(VIEW_ITEM_NAME, packageInfo.packageName);

                    // Add if app is selected
                    boolean isChecked = ignoreList.contains(packageInfo.packageName);
                    packageItem.put(VIEW_ITEM_CHECKBOX, isChecked);
                    
                    // Add to package list
                    int countVariable = (isChecked ? 1 : 0);
                    if (Util.isSystemApp(packageInfo.applicationInfo)) {                        
                        mSystemAppList.add(packageItem);
                        mSystemAppSelectedCount += countVariable;
                    } else {
                        mPersonalAppList.add(packageItem);
                        mPersonalAppSelectedCount += countVariable;
                    }
                }
            }
        }        

        private void sortPackageList() {
            // Sort package list in alphabetical order.
            PackageItemComparator comparator = new PackageItemComparator();
            
            // Sort personal app list
            if (mPersonalAppList != null) {
                Collections.sort(mPersonalAppList, comparator);
            }
            
            // Sort system app list
            if (mSystemAppList != null) {
                Collections.sort(mSystemAppList, comparator);
            }
        }        
    }    
}
