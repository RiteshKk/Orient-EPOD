package com.ipssi.orient_epod.capturesignature.viewUtil

import android.os.Build
import android.view.View

object ViewCompat {

  fun isLaidOut(view: View): Boolean {
    return if (Build.VERSION.SDK_INT >= 19) {
      view.isLaidOut
    } else view.width > 0 && view.height > 0
  }
}