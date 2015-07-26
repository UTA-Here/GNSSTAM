package com.yunnanexplorer.android.ygps;

/*
Copyright (C) 2009-2010  Ludwig M Brinckmann <ludwigbrinckmann@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DataView extends BaseDataView {

	TextView mData;
	
	public DataView(Context context){
		this(context, null);
	}
	
	public DataView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mData = new TextView(getContext());
		mData.setTextColor(YGPSConstants.dataviewTextColor);
		mData.setGravity(Gravity.FILL_HORIZONTAL);
		mData.setBackgroundColor(YGPSConstants.dataviewTextFieldBgColor);
		mData.setTextSize(YGPSConstants.dataviewTextSize);
		mData.setGravity(Gravity.RIGHT);		
		addView(mData, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		addLegend();
	}

	public void setData(CharSequence text){
		mData.setText(text);
		mData.postInvalidate();
	}

	public void setData(Double d){
		mData.setText(mFormat.format(d));
		mData.postInvalidate();
	}
	public void setData(Float d){
		mData.setText(Float.toString(d));
		mData.postInvalidate();
	}

	public void setData(int d) {
		mData.setText(Integer.toString(d));
		mData.postInvalidate();
	}

	public void setData(long d) {
		mData.setText(Long.toString(d));
		mData.postInvalidate();
	}

}