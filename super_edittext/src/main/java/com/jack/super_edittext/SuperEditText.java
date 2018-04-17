package com.jack.super_edittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @describe 带有动画效果，具有清除和扫码功能的EditText
 * 作者：Tuo on 2018/4/17 15:28
 * 邮箱：839539179@qq.com
 */

public class SuperEditText extends ConstraintLayout {


    // 清除按钮
    private int set_iv_clear_width, set_iv_clear_height;
    private Drawable set_iv_clear_src;
    private int set_iv_clear_padding, set_iv_clear_marginRight;


    // 扫描按钮
    private int set_iv_scan_width, set_iv_scan_height;
    private Drawable set_iv_scan_src;
    private int set_iv_scan_padding, set_iv_scan_marginRight;

    private Drawable set_et_bg, set_et_left_drawable;
    private int set_et_drawablePadding, set_et_marginRight, set_et_marginLeft, set_et_paddingLeft;
    private boolean set_et_cursorVisible, set_et_focusable;
    private String set_et_hint_text, set_et_text;
    private float set_et_hint_text_size, set_et_text_size;
    private int set_et_hint_text_color, set_et_text_color;


    private ConstraintSet mConstraintSet;
    private ConstraintLayout constraintLayout;
    private ImageView ivClear, ivScan;
    private EditText et;

    //标记是否启用扫描功能
    private boolean hasScan;

    /**
     * et控件是否有焦点
     */
    private boolean mHasFocus;

    private OnClickListener mOnClickListener;

    private OnSearchListener mOnSearchListener;

    private OnScanListener mOnScanListener;

    public SuperEditText(Context context) {
        this(context, null);
    }

    public SuperEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SuperEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }




    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        ConstraintLayout.inflate(context, R.layout.layout_super_edit_text, this);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SuperEditText, defStyleAttr, 0);
        // 是否启用扫描功能 默认不启用
        hasScan = array.getBoolean(R.styleable.SuperEditText_set_has_scan, false);
        // imageView clear 清除按钮的图片
        set_iv_clear_width = array.getDimensionPixelSize(R.styleable.SuperEditText_set_iv_clear_width, 0);
        set_iv_clear_height = array.getDimensionPixelSize(R.styleable.SuperEditText_set_iv_clear_height, 0);
        set_iv_clear_src = array.getDrawable(R.styleable.SuperEditText_set_iv_clear_src);
        set_iv_clear_padding = array.getDimensionPixelSize(R.styleable.SuperEditText_set_iv_clear_padding, 0);
        set_iv_clear_marginRight = array.getDimensionPixelSize(R.styleable.SuperEditText_set_iv_clear_marginRight, 0);

        // imageView scan 扫描的图片
        set_iv_scan_width = array.getDimensionPixelSize(R.styleable.SuperEditText_set_iv_scan_width, 0);
        set_iv_scan_height = array.getDimensionPixelSize(R.styleable.SuperEditText_set_iv_scan_height, 0);
        set_iv_scan_src = array.getDrawable(R.styleable.SuperEditText_set_iv_scan_src);
        set_iv_scan_padding = array.getDimensionPixelSize(R.styleable.SuperEditText_set_iv_scan_padding, 0);
        set_iv_scan_marginRight = array.getDimensionPixelSize(R.styleable.SuperEditText_set_iv_scan_marginRight, 0);

        //EditText
        set_et_bg = array.getDrawable(R.styleable.SuperEditText_set_et_bg);
        set_et_left_drawable = array.getDrawable(R.styleable.SuperEditText_set_et_left_drawable);
        set_et_cursorVisible = array.getBoolean(R.styleable.SuperEditText_set_et_cursorVisible, true);
        set_et_focusable = array.getBoolean(R.styleable.SuperEditText_set_et_focusable, false);

        set_et_marginLeft = array.getDimensionPixelSize(R.styleable.SuperEditText_set_et_marginLeft, 0);
        set_et_marginRight = array.getDimensionPixelSize(R.styleable.SuperEditText_set_et_marginRight, 0);
        set_et_drawablePadding = array.getDimensionPixelSize(R.styleable.SuperEditText_set_et_drawablePadding, 0);

        set_et_paddingLeft = array.getDimensionPixelSize(R.styleable.SuperEditText_set_et_paddingLeft, 0);
        set_et_hint_text = array.getString(R.styleable.SuperEditText_set_et_hint_text);
        set_et_text = array.getString(R.styleable.SuperEditText_set_et_text);
        set_et_hint_text_size = array.getDimensionPixelOffset(R.styleable.SuperEditText_set_et_hint_text_size, 14);
        set_et_text_size = array.getDimensionPixelOffset(R.styleable.SuperEditText_set_et_text_size, 14);
        set_et_hint_text_color = array.getColor(R.styleable.SuperEditText_set_et_hint_text_color, Color.BLACK);
        set_et_text_color = array.getColor(R.styleable.SuperEditText_set_et_text_color, Color.BLACK);


        array.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraint_layout);
        ivClear = (ImageView) findViewById(R.id.iv_clear);
        ivScan = (ImageView) findViewById(R.id.iv_scan);
        et = (EditText) findViewById(R.id.et);

        setImageViewClearValue();
        setImageViewScanValue();
        setEditTextValue();

        mConstraintSet = new ConstraintSet();
        mConstraintSet.clone(constraintLayout);

        initListener();

    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public void setOnSearchListener(OnSearchListener searchListener) {
        this.mOnSearchListener = searchListener;
    }

    public void setOnScanListener(OnScanListener onScanListener) {
        this.mOnScanListener = onScanListener;
    }


    private void setImageViewClearValue() {
        ivClear.getLayoutParams().width = set_iv_clear_width;
        ivClear.getLayoutParams().height = set_iv_clear_height;
        ivClear.setImageDrawable(set_iv_clear_src);
        ivClear.setPadding(set_iv_clear_padding, set_iv_clear_padding, set_iv_clear_padding, set_iv_clear_padding);
        setMargins(ivClear, 0, 0, set_iv_clear_marginRight, 0);
    }


    private void setImageViewScanValue() {
        ivScan.getLayoutParams().width = set_iv_scan_width;
        ivScan.getLayoutParams().height = set_iv_scan_height;
        ivScan.setImageDrawable(set_iv_scan_src);
        ivScan.setPadding(set_iv_scan_padding, set_iv_scan_padding, set_iv_scan_padding, set_iv_scan_padding);
        setMargins(ivScan, 0, 0, set_iv_scan_marginRight, 0);
    }

    private void setEditTextValue() {
        et.setBackgroundDrawable(set_et_bg);
        if (set_et_left_drawable != null) {
            set_et_left_drawable.setBounds(0, 0, set_et_left_drawable.getMinimumWidth(), set_et_left_drawable.getMinimumHeight());
            et.setCompoundDrawables(set_et_left_drawable, null, null, null);
        }
        et.setCompoundDrawablePadding(set_et_drawablePadding);
        setMargins(et, set_et_marginLeft, 0, set_et_marginRight, 0);
        et.setPadding(set_et_paddingLeft, 0, 0, 0);
        et.setCursorVisible(set_et_cursorVisible);
        et.setFocusable(set_et_focusable);
        et.setText(set_et_text);
        et.setHintTextColor(set_et_hint_text_color);
        et.setTextColor(set_et_text_color);
        et.setTextSize(TypedValue.COMPLEX_UNIT_PX, set_et_text_size);
        if (set_et_hint_text != null) {
            SpannableString ss = new SpannableString(set_et_hint_text);//定义hint的值
            AbsoluteSizeSpan ass = new AbsoluteSizeSpan((int) set_et_hint_text_size, false);//设置字体大小 true表示单位是sp
            ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            et.setHint(new SpannedString(ss));
        }
    }


    public EditText getEt() {
        return et;
    }


    private void initListener() {
        //默认设置隐藏图标
        setClearIconVisible(false);
        //清除按钮监听
        ivClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                et.setText("");
            }
        });

        ivScan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnScanListener != null) {
                    mOnScanListener.scan();
                }
            }
        });
        // 设置EditText点击之后的动画 以及焦点设置
        et.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(constraintLayout);
                }
            }
        });

        constraintLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(constraintLayout);
                }
            }
        });


        //设置输入框里面内容发生改变的监听
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mHasFocus) {
                    setClearIconVisible(charSequence.length() > 0);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // 焦点变化监听
        et.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                mHasFocus = hasFocus;
                if (hasFocus) {
                    setClearIconVisible(et.getText().length() > 0);
                } else {
                    setClearIconVisible(false);
                }
            }
        });


        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (mOnSearchListener != null) {
                        mOnSearchListener.search(getInputContent());
                    }
                    return true;
                }
                return false;
            }
        });
    }



    public void showAnimal() {
        TransitionManager.beginDelayedTransition(constraintLayout);
        mConstraintSet.clear(R.id.et);

        mConstraintSet.connect(R.id.et, ConstraintSet.LEFT, R.id.constraint_layout, ConstraintSet.LEFT, 1000);
        mConstraintSet.connect(R.id.et, ConstraintSet.RIGHT, R.id.iv_clear, ConstraintSet.LEFT);
        mConstraintSet.connect(R.id.et, ConstraintSet.TOP, R.id.constraint_layout, ConstraintSet.TOP);
        mConstraintSet.connect(R.id.et, ConstraintSet.BOTTOM, R.id.constraint_layout, ConstraintSet.BOTTOM);

        mConstraintSet.applyTo(constraintLayout);


    }

    public void setCetCanInput() {
        et.setEnabled(true);
        et.setFocusable(true);//可以通过键盘得到焦点
        et.setFocusableInTouchMode(true);//可以通过触摸得到焦点
        showKeyboard(et);
        ivClear.setVisibility(GONE);
    }

    /**
     * 显示虚拟键盘
     */
    public  void showKeyboard(View v) {
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
    }


    public String getInputContent() {
        String content = et.getText().toString().trim();
        return content;
    }


    /**
     * 设置清除图标的显示与隐藏
     *
     * @param visible
     */
    protected void setClearIconVisible(boolean visible) {
        ivClear.setVisibility(visible == true ? View.VISIBLE : View.INVISIBLE);
        ivScan.setVisibility(visible == false ? View.VISIBLE : View.INVISIBLE);
    }

    public interface OnSearchListener {
        void search(String searchContent);
    }

    public interface OnScanListener {
        void scan();
    }


    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof MarginLayoutParams) {
            MarginLayoutParams p = (MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }
}
