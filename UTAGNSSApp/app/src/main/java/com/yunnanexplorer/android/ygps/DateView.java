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
import android.text.format.Time;
import android.util.AttributeSet;

public class DateView extends DataView {

	Time mTime;
	
	public DateView(Context context){
		this(context, null);
	}
	
	public DateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTime = new Time();
	}

	public void setData(long d) {
		mTime.set(d);
		mData.setText(mTime.format3339(false));
		mData.postInvalidate();
	}

}