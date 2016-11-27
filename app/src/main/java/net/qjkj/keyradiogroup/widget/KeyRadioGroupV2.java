package net.qjkj.keyradiogroup.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

/**
 * Created by Key on 2015/9/27 16:31
 * email: MrKey.K@gmail.com
 * description: 此版本只能支持内部一层布局，为了防止意外BUG，不要多层布局，RadioGroup内放一层布局，然后再在布局内放RadioButton，并且只放RadioButton
 *              此版本只是为了增加点效率。因为大部分情况下使用不需要那么多层布局
 *              V2单独一个RadioButton是不支持的，就算单独一个外面也要再加一层布局，因为本身就是为了简化代码增加效率
 *              考虑到实际这样的用法不多和初衷只是为了实现多行多列。所以懒得再写一个else if了
 *              如果布局很复杂直接用V1就好了。
 * @version 1.2
 */

public class KeyRadioGroupV2 extends LinearLayout {
    private int mCheckedId = -1;
    // tracks children radio buttons checked state
    private CompoundButton.OnCheckedChangeListener mChildOnCheckedChangeListener;
    // when true, mOnCheckedChangeListener discards events
    private boolean mProtectFromCheckedChange = false;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private PassThroughHierarchyChangeListener mPassThroughListener;

    public KeyRadioGroupV2(Context context) {
        super(context);
        setOrientation(VERTICAL);
        init();
    }

    public KeyRadioGroupV2(Context context, AttributeSet attrs) {
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
     * 父类之会识别是RadioButton的子控件，所以这个RadioGroup必须在下面放一个别的布局才会不重复添加
     * 为了防止意外BUG，不要多层布局，RadioGroup内放一层布局，然后再在布局内放RadioButton，并且只放RadioButton
     * 为了不影响效率。并且只是满足简单的使用，就没有把方法抽出来，此版本只能支持内部一层布局
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof ViewGroup) {
            int childCount = ((ViewGroup) child).getChildCount();
            for (int i = 0; i < childCount; i++) {
                final RadioButton button = (RadioButton) ((ViewGroup) child).getChildAt(i);
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
     * <p>Sets the selection to the radio button whose identifier is passed in
     * parameter. Using -1 as the selection identifier clears the selection;
     * such an operation is equivalent to invoking {@link #clearCheck()}.</p>
     *
     * @param id the unique id of the radio button to select in this group
     *
     * @see #getCheckedRadioButtonId()
     * @see #clearCheck()
     */
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

    /**
     * <p>Returns the identifier of the selected radio button in this group.
     * Upon empty selection, the returned value is -1.</p>
     *
     * @return the unique id of the selected radio button in this group
     *
     * @see #check(int)
     * @see #clearCheck()
     *
     * @attr ref android.R.styleable#RadioGroup_checkedButton
     */
    @IdRes
    public int getCheckedRadioButtonId() {
        return mCheckedId;
    }

    /**
     * <p>Clears the selection. When the selection is cleared, no radio button
     * in this group is selected and {@link #getCheckedRadioButtonId()} returns
     * null.</p>
     *
     * @see #check(int)
     * @see #getCheckedRadioButtonId()
     */
    public void clearCheck() {
        check(-1);
    }

    /**
     * <p>Register a callback to be invoked when the checked radio button
     * changes in this group.</p>
     *
     * @param listener the callback to call on checked state change
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    @Override
    public KeyRadioGroupV2.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new KeyRadioGroupV2.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof KeyRadioGroupV2.LayoutParams;
    }

    @Override
    protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
        return new KeyRadioGroupV2.LayoutParams(KeyRadioGroupV2.LayoutParams.WRAP_CONTENT, KeyRadioGroupV2.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return KeyRadioGroupV2.class.getName();
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

        /**
         * <p>Fixes the child's width to
         * {@link ViewGroup.LayoutParams#WRAP_CONTENT} and the child's
         * height to  {@link ViewGroup.LayoutParams#WRAP_CONTENT}
         * when not specified in the XML file.</p>
         *
         * @param a the styled attributes set
         * @param widthAttr the width attribute to fetch
         * @param heightAttr the height attribute to fetch
         */
        @Override
        protected void setBaseAttributes(TypedArray a,
                                         int widthAttr, int heightAttr) {

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

    /**
     * <p>Interface definition for a callback to be invoked when the checked
     * radio button changed in this group.</p>
     */
    public interface OnCheckedChangeListener {
        /**
         * <p>Called when the checked radio button has changed. When the
         * selection is cleared, checkedId is -1.</p>
         *
         * @param group the group in which the checked radio button has changed
         * @param checkedId the unique identifier of the newly checked radio button
         */
        public void onCheckedChanged(KeyRadioGroupV2 group, @IdRes int checkedId);
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

    private class PassThroughHierarchyChangeListener implements
            OnHierarchyChangeListener {
        private OnHierarchyChangeListener mOnHierarchyChangeListener;

        /**
         * 为了不出意料之外的BUG。所以强行规定了。RadioGroup内必须加一层布局
         */
        public void onChildViewAdded(View parent, View child) {
            if (parent == KeyRadioGroupV2.this && child instanceof ViewGroup) {
                int childCount = ((ViewGroup) child).getChildCount();
                for (int i = 0; i < childCount; i++) {
                    int id = ((ViewGroup) child).getChildAt(i).getId();
                    // generates an id if it's missing
                    if (id == View.NO_ID) {
                        id = View.generateViewId(); //这个方法最低需要API17
                        ((ViewGroup) child).getChildAt(i).setId(id);
                    }
                    ((RadioButton) ((ViewGroup) child).getChildAt(i)).setOnCheckedChangeListener(
                            mChildOnCheckedChangeListener);
                }
            }

            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        /**
         * 移除同上
         */
        public void onChildViewRemoved(View parent, View child) {
            if (parent == KeyRadioGroupV2.this && child instanceof ViewGroup) {
                int childCount = ((ViewGroup) child).getChildCount();
                for (int i = 0; i < childCount; i++) {
                    ((RadioButton)  ((ViewGroup) child).getChildAt(i)).setOnCheckedChangeListener(null);
                }
            }

            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }
}
