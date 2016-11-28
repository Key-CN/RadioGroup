package net.qjkj.keyradiogroup.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Key on 2015/8/13 12:29
 * email: MrKey.K@gmail.com
 * description: 重写原生RadioGroup，用集合实现RadioGroup内可以再布局，从而实现多行多列的RadioGroup
 * {getAllRadioButton}
 * {addView}
 * @version 1.2
 * {@link PassThroughHierarchyChangeListener }
 */

public class KeyRadioGroupV1 extends LinearLayout {
    // holds the checked id; the selection is empty by default
    private int mCheckedId = -1;
    // tracks children radio buttons checked state
    private CompoundButton.OnCheckedChangeListener mChildOnCheckedChangeListener;
    // when true, mOnCheckedChangeListener discards events
    private boolean mProtectFromCheckedChange = false;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private PassThroughHierarchyChangeListener mPassThroughListener;

    public KeyRadioGroupV1(Context context) {
        super(context);
        setOrientation(VERTICAL);
        init();
    }

    public KeyRadioGroupV1(Context context, AttributeSet attrs) {
        super(context, attrs);
        // retrieve selected radio button as requested by the user in the
        // XML layout file

        // com.android.internal.R.styleable.RadioGroup
        // com.android.internal.R.attr.radioButtonStyle
        TypedArray attributes = context.obtainStyledAttributes(attrs,
                new int[]{Resources.getSystem().getIdentifier("RadioGroup", "styleable", "android")},
                Resources.getSystem().getIdentifier("radioButtonStyle", "attr", "android"), 0);

        // com.android.internal.R.styleable.RadioGroup_checkedButton
        int value = attributes.getResourceId(Resources.getSystem().getIdentifier("RadioGroup_checkedButton", "styleable", "android"), View.NO_ID);
        if (value != View.NO_ID) {
            mCheckedId = value;
        }

        // com.android.internal.R.styleable.RadioGroup_orientation
        final int index = attributes.getInt(Resources.getSystem().getIdentifier("RadioGroup_orientation", "styleable", "android"), VERTICAL);
        setOrientation(index);

        attributes.recycle();
        init();
    }

    private void init() {
        mChildOnCheckedChangeListener = new CheckedStateTracker();
        mPassThroughListener = new PassThroughHierarchyChangeListener();
        super.setOnHierarchyChangeListener(mPassThroughListener);
    }


    @Override
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        // the user listener is delegated to our pass-through listener
        mPassThroughListener.mOnHierarchyChangeListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // checks the appropriate radio button as requested in the XML file
        if (mCheckedId != -1) {
            mProtectFromCheckedChange = true;
            setCheckedStateForView(mCheckedId, true);
            mProtectFromCheckedChange = false;
            setCheckedId(mCheckedId);
        }
    }

    /**
     * 每次添加view 就递归寻找是否含有RadioButton
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        List<RadioButton> allRadioButton = getAllRadioButton(child);
        if(allRadioButton != null && allRadioButton.size() > 0){
            for(RadioButton button : allRadioButton){
                if (button.isChecked()) {
                    mProtectFromCheckedChange = true;
                    if (mCheckedId != -1) {
                        setCheckedStateForView(mCheckedId, false);
                    }
                    mProtectFromCheckedChange = false;
                    setCheckedId(button.getId());
                }
            }
        }
        super.addView(child, index, params);
    }

    /**
     * 递归寻找{@link RadioButton}
     */
    private List<RadioButton> getAllRadioButton(View child){
        List<RadioButton> allRadioButton = new ArrayList<>();
        if (child instanceof RadioButton) {
            allRadioButton.add((RadioButton) child);
        }else if(child instanceof ViewGroup){
            int counts = ((ViewGroup) child).getChildCount();
            for(int i = 0; i < counts; i++){
                // 递归，把返回集合中的内容全部加入到外层的集合，然后返回
                allRadioButton.addAll(getAllRadioButton(((ViewGroup) child).getChildAt(i)));
            }
        }
        return allRadioButton;
    }

    public void check(@IdRes int id) {
        // don't even bother
        if (id != -1 && (id == mCheckedId)) {
            return;
        }
        if (mCheckedId != -1) {
            setCheckedStateForView(mCheckedId, false);
        }
        if (id != -1) {
            setCheckedStateForView(id, true);
        }
        setCheckedId(id);
    }

    private void setCheckedId(@IdRes int id) {
        mCheckedId = id;
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, mCheckedId);
        }
    }

    private void setCheckedStateForView(int viewId, boolean checked) {
        View checkedView = findViewById(viewId);
        if (checkedView != null && checkedView instanceof RadioButton) {
            ((RadioButton) checkedView).setChecked(checked);
        }
    }

    @IdRes
    public int getCheckedRadioButtonId() {
        return mCheckedId;
    }


    public void clearCheck() {
        check(-1);
    }


    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new KeyRadioGroupV1.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof KeyRadioGroupV1.LayoutParams;
    }

    @Override
    protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return KeyRadioGroupV1.class.getName();
    }

    public static class LayoutParams extends LinearLayout.LayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(int w, int h, float initWeight) {
            super(w, h, initWeight);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @Override
        protected void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
            if (a.hasValue(widthAttr)) {
                width = a.getLayoutDimension(widthAttr, "layout_width");
            } else {
                width = WRAP_CONTENT;
            }
            if (a.hasValue(heightAttr)) {
                height = a.getLayoutDimension(heightAttr, "layout_height");
            } else {
                height = WRAP_CONTENT;
            }
        }
    }


    public interface OnCheckedChangeListener {
        public void onCheckedChanged(KeyRadioGroupV1 group, @IdRes int checkedId);
    }

    private class CheckedStateTracker implements CompoundButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // prevents from infinite recursion
            if (mProtectFromCheckedChange) {
                return;
            }

            mProtectFromCheckedChange = true;
            if (mCheckedId != -1) {
                setCheckedStateForView(mCheckedId, false);
            }
            mProtectFromCheckedChange = false;

            int id = buttonView.getId();
            setCheckedId(id);
        }
    }

    /**
     * 从集合中去获取，不重写之前多行多列等复杂布局只会返回-1，所以实现的重点还是在于重写这个监听，递归获取
     */
    private class PassThroughHierarchyChangeListener implements
            OnHierarchyChangeListener {
        private OnHierarchyChangeListener mOnHierarchyChangeListener;

        @SuppressLint("NewApi")
        public void onChildViewAdded(View parent, View child) {
            if (parent == KeyRadioGroupV1.this ) {
                List<RadioButton> allRadioButton = getAllRadioButton(child);
                if(allRadioButton != null && allRadioButton.size() > 0){
                    for(RadioButton button : allRadioButton){
                        int id = button.getId();
                        // generates an id if it's missing
                        if (id == View.NO_ID) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                // generateViewId() 这个方法最低需要API17 这个方法用于生成ID , 这里做一下向下兼容
                                id = View.generateViewId();
                            } else {
                                id = child.hashCode();
                            }
                            button.setId(id);
                        }
                        // 不知道为什么setOnCheckedChangeWidgetListener方法不能调 RadioButton 的父类CompoundButton中是有这个方法的
                        button.setOnCheckedChangeListener(
                                mChildOnCheckedChangeListener);
                    }
                }
            }
            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        public void onChildViewRemoved(View parent, View child) {
            if (parent == KeyRadioGroupV1.this) {
                List<RadioButton> allRadioButton = getAllRadioButton(child);
                if(allRadioButton != null && allRadioButton.size() > 0){
                    for(RadioButton button : allRadioButton){
                        button.setOnCheckedChangeListener(null);
                    }
                }
            }
            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }
}
