package com.example.mylibrary.todo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AnyRes;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import com.example.mylibrary.R;
import com.example.mylibrary.base.TopBarActivity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class UiUtils {

  public static void renderEditText(final EditText edit, final View del) {
    del.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        edit.setText("");
      }
    });
    edit.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
      }

      @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
      }

      @Override public void afterTextChanged(Editable editable) {
        del.setVisibility(editable.length() > 0 ? View.VISIBLE : View.INVISIBLE);
      }
    });
    edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override public void onFocusChange(View v, boolean hasFocus) {
        del.setVisibility(hasFocus && edit.getText().length() > 0 ? View.VISIBLE : View.INVISIBLE);
      }
    });
  }

  /**
   * 限制输入小数点后两位小数
   */
  public static void setDecimalDiaitsPoint(final EditText editText) {
    editText.addTextChangedListener(new TextWatcher() {

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().contains(".")) {
          if (s.length() - 1 - s.toString().indexOf(".") > 2) {
            s = s.toString().subSequence(0, s.toString().indexOf(".") + 3);
            editText.setText(s);
            editText.setSelection(s.length());
          }
        }
        if (s.toString().trim().substring(0).equals(".")) {
          s = "0" + s;
          editText.setText(s);
          editText.setSelection(2);
        }

        if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
          if (!s.toString().substring(1, 2).equals(".")) {
            editText.setText(s.subSequence(0, 1));
            editText.setSelection(1);
            return;
          }
        }
      }

      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override public void afterTextChanged(Editable s) {
      }
    });
  }

  public static TextView setCenterTitle(Activity activity, Toolbar toolbar, int titleRes) {
    return setCenterTitle(activity, toolbar, activity.getString(titleRes));
  }

  public static TextView setCenterTitle(Activity activity, Toolbar toolbar, String title) {
    TextView titleView =
        (TextView) activity.getLayoutInflater().inflate(R.layout.header_title, toolbar, false);
    titleView.setSingleLine();
    titleView.setEllipsize(TextUtils.TruncateAt.END);
    titleView.setText(title);
    Toolbar.LayoutParams lp = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    lp.gravity = Gravity.CENTER;
    toolbar.addView(titleView, lp);
    return titleView;
  }

  /**
   * 根据属性ID获取对应的style文件的资源
   *
   * @param context {@link Context}
   * @param attr {@link AttrRes} 属性值eg:{@link R.attr#selectableItemBackground}
   * @return {@link AnyRes} 资源ID eg:{@link R.drawable#abc_list_focused_holo}
   */
  @AnyRes public static int getAttribute(@NonNull Context context, @AttrRes int attr) {
    final TypedValue mTypedValue = new TypedValue();
    context.getTheme().resolveAttribute(attr, mTypedValue, true);
    return mTypedValue.resourceId;
  }

  /**
   * @param activity {@link TopBarActivity}
   * @param isLight true: 状态栏,白色背景, 深色文字
   */
  public static void requestStatusBarLight(TopBarActivity activity, boolean isLight) {
    requestStatusBarLight(activity, isLight, isLight ? 0xffcccccc : 0xffffffff);
  }

  /**
   * @param activity {@link TopBarActivity}
   * @param isLight 6.0及以上系统生效(true: 状态栏,白色背景, 深色文字)
   * @param color 6.0以下系统生效(自定义的状态栏背景色)
   */
  public static void requestStatusBarLight(TopBarActivity activity, boolean isLight, int color) {
    View decorView = activity.getWindow().getDecorView();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (isLight) {
        decorView.setSystemUiVisibility(
            decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
      } else {
        decorView.setSystemUiVisibility(
            decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
      }
      processPrivateAPI(activity.getWindow(), isLight);
    } else {
      activity.getStatusBar().setBackgroundColor(color);
    }
  }

  private static void processPrivateAPI(Window window, boolean lightStatusBar) {
    try {
      processFlyMe(window, lightStatusBar);
    } catch (Exception e) {
      try {
        processMIUI(window, lightStatusBar);
      } catch (Exception e2) {
        //
      }
    }
  }

  /**
   * 改变小米的状态栏字体颜色为黑色, 要求MIUI6以上
   * Tested on: MIUIV7 5.0 Redmi-Note3
   *
   * @param window {@link Window}
   * @param lightStatusBar
   * @throws Exception
   */
  private static void processMIUI(Window window, boolean lightStatusBar) throws Exception {
    Class<? extends Window> clazz = window.getClass();
    int darkModeFlag;
    Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
    Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
    darkModeFlag = field.getInt(layoutParams);
    Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
    extraFlagField.invoke(window, lightStatusBar ? darkModeFlag : 0, darkModeFlag);
  }

  /**
   * 改变魅族的状态栏字体为黑色，要求FlyMe4以上
   *
   * @param window
   * @param isLightStatusBar
   * @throws Exception
   */
  private static void processFlyMe(Window window, boolean isLightStatusBar) throws Exception {
    WindowManager.LayoutParams lp = window.getAttributes();
    Class<?> instance = Class.forName("android.view.WindowManager$LayoutParams");
    int value = instance.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON").getInt(lp);
    Field field = instance.getDeclaredField("meizuFlags");
    field.setAccessible(true);
    int origin = field.getInt(lp);
    if (isLightStatusBar) {
      field.set(lp, origin | value);
    } else {
      field.set(lp, (~value) & origin);
    }
  }

  /**
   * @param dialog
   * @param statusBar
   * @param isLight true: 白色背景, 深色文字
   */
  public static void requestStatusBarLightForDialog(Dialog dialog, View statusBar,
      boolean isLight) {
    Window window = dialog.getWindow();
    View decorView = window.getDecorView();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (isLight) {
        decorView.setSystemUiVisibility(
            decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
      } else {
        decorView.setSystemUiVisibility(
            decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
      }
      processPrivateAPI(window, isLight);
      if (isLight) {
        statusBar.setBackgroundColor(0xffffffff);
      } else {
        statusBar.setBackgroundColor(0xffcccccc);
      }
    } else {
      if (isLight) {
        statusBar.setBackgroundColor(0xffcccccc);
      } else {
        statusBar.setBackgroundColor(0xffffffff);
      }
    }
  }
}
