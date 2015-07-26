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

import java.text.DecimalFormat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BaseDataView extends LinearLayout {
	protected Context mContext;
	protected LinearLayout mLegend;
	protected TextView mDescription;
	protected TextView mUnits;
	protected DecimalFormat mFormat;

	
	public BaseDataView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mFormat = new DecimalFormat("#.######");
		setBackgroundColor(YGPSConstants.dataviewBgColor);
				
		setPadding(5,5,5,5);
		setOrientation(LinearLayout.VERTICAL);
		mDescription = new TextView(getContext());
		mDescription.setBackgroundColor(YGPSConstants.dataviewLegendFieldBgColor);
		mDescription.setGravity(Gravity.LEFT);
		mDescription.setTextColor(YGPSConstants.dataviewTextColor);
		mUnits = new TextView(getContext());
		mUnits.setBackgroundColor(YGPSConstants.dataviewLegendFieldBgColor);
		mUnits.setGravity(Gravity.RIGHT);
		mUnits.setTextColor(YGPSConstants.dataviewTextColor);
		mLegend = new LinearLayout(getContext());
		mLegend.setOrientation(LinearLayout.HORIZONTAL);
		mLegend.setBackgroundColor(YGPSConstants.dataviewLegendFieldBgColor);
	}

	protected void addLegend(){
		mLegend.addView(mDescription, new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.FILL_PARENT,1));
		mLegend.addView(mUnits, new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.FILL_PARENT,1));
		addView(mLegend, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));		
	}
	
	public void setUnits(CharSequence text){
		mUnits.setText(text);
		mUnits.postInvalidate();
	}

	public void setDescription(CharSequence text){
		mDescription.setText(text);
		mDescription.postInvalidate();
	}

	public void setFormatting(String d){
		mFormat = new DecimalFormat(d);
	}
	
}
