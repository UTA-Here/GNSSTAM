package com.utahere.gnssapp;

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

import com.yunnanexplorer.android.ygps.IconView;

public class GPSStateView extends IconView {

	public GPSStateView(Context context){
		this(context, null);
	}
	
	public GPSStateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDescription("GPS State");
	}
	
	void stateOff(){
		setImageResource(R.drawable.satred);
		setUnits("off");
	}
	
	void stateOn(){
		setImageResource(R.drawable.satyellow);
		setUnits("no lock");
	}
	
	void stateLock(){
		setImageResource(R.drawable.satgreen);
		setUnits("lock");
	}
}

