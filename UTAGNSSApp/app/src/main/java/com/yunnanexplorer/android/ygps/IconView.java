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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IconView extends BaseDataView {

	ImageView mData;
	/* dummy is used to force height of field to height of text fields */
	TextView mDummy;
	LinearLayout mDataWrapper;
	
	public IconView(Context context){
		this(context, null);
	}
	
	public IconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mData = new ImageView(getContext());
		mData.setBackgroundColor(YGPSConstants.dataviewTextFieldBgColor);
		mDummy = new TextView(getContext());
		mDummy.setBackgroundColor(YGPSConstants.dataviewTextFieldBgColor);
		mDummy.setTextSize(YGPSConstants.dataviewTextSize);
		mDataWrapper = new LinearLayout(getContext());
		mDataWrapper.setOrientation(LinearLayout.HORIZONTAL);
		mDataWrapper.setBackgroundColor(YGPSConstants.dataviewTextFieldBgColor);
		
		mDataWrapper.addView(mData, new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.FILL_PARENT,10000));
		mDataWrapper.addView(mDummy, new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.FILL_PARENT,1));
		addView(mDataWrapper, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		addLegend();		
	}
	
	public void setImageResource(int drawable){
		mData.setImageResource(drawable);
	}
}