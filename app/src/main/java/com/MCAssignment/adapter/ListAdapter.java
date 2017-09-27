package com.MCAssignment.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.MCAssignment.wifi.R;

import java.util.List;

public class ListAdapter extends ArrayAdapter<String> {
	Activity mActivity;
	List<String> wifiName;
	private LayoutInflater inflater;

	public ListAdapter(Activity a, List<String> wifiName) {
		super(a, R.layout.single_list, wifiName);
		mActivity = a;
		inflater = LayoutInflater.from(mActivity);
		this.wifiName = wifiName;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = inflater.inflate(R.layout.single_list, parent, false);
		TextView wifiProvider = (TextView) convertView.findViewById(R.id.txt_wifi_provider);
		wifiProvider.setText(wifiName.get(position));
		return convertView;
	}

}
