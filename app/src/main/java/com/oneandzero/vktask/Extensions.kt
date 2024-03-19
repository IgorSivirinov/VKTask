package com.oneandzero.vktask

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import java.security.AccessController.getContext

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt();
