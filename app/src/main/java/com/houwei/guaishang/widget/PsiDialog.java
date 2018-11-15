package com.houwei.guaishang.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.houwei.guaishang.R;
import com.houwei.guaishang.util.DeviceUtils;


/**
 * Psi对话框
 */
public class PsiDialog extends Dialog {


    private TextView mTitleTextView;// 标题
    private TextView mContentTextView;// 正文内容
    private Button mPositiveBtn;
    private Button mNegativeBtn;
    private Context mContext;
    // ---------------------------------构造函数

    /**
     * 显示正文，标题
     *
     * @param ctx
     * @param content
     * @param title
     */
    public PsiDialog(Context ctx, int content, int title) {
        this(ctx, ctx.getString(content), ctx.getString(title), true);
    }


    /**
     * 只显示正文内容
     *
     * @param ctx
     * @param content
     */
    public PsiDialog(Context ctx, int content) {
        this(ctx, ctx.getString(content), "", true);
    }

    /**
     * 只显示正文内容---重载
     *
     * @param ctx
     * @param content
     */
    public PsiDialog(Context ctx, String content) {
        this(ctx, content, "", true);
    }


    public PsiDialog(Context ctx, int content, int title, boolean cancelAble) {
        this(ctx, ctx.getString(content), ctx.getString(title), cancelAble);
    }


    public PsiDialog(Context context, String content, String title, boolean cancelAble) {
        super(context, R.style.PsiDialogTheme);
        mContext = context;
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_psi, null);
        mTitleTextView = (TextView) contentView.findViewById(R.id.title_textview);
        if (title != null && !title.isEmpty()) {
            mTitleTextView.setVisibility(View.VISIBLE);
            mTitleTextView.setText(title);
        } else {
            mTitleTextView.setVisibility(View.GONE);
        }

        // 正文内容
        mContentTextView = (TextView) contentView.findViewById(R.id.content_textview);
        mContentTextView.setText(content);

        // 设置TextView可以滚动
        mContentTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        // Positive Button
        mPositiveBtn = (Button) contentView.findViewById(R.id.btn_positive);
        // Negative Button
        mNegativeBtn = (Button) contentView.findViewById(R.id.btn_negative);

        setCancelable(cancelAble);

        setContentView(contentView);
        WindowManager.LayoutParams windowparams = getWindow().getAttributes();
        getWindow().setGravity(Gravity.CENTER);
        Rect rect = new Rect();
        View decorView = getWindow().getDecorView();
        decorView.getWindowVisibleDisplayFrame(rect);

        windowparams.width = DeviceUtils.getScreenWid(mContext)- DeviceUtils.dip2px(mContext, 86f);
        getWindow().setAttributes(windowparams);
    }


    public interface OnPositiveButtonClickListener {
        void onPositiveButtonClick(View view);
    }

    public interface OnNegativeButtonClickListener {
        void onNegativeButtonClick(View view);
    }

    public interface OnButtonClickListener extends
            OnPositiveButtonClickListener, OnNegativeButtonClickListener {
    }

    private OnButtonClickListener mListener;

    /**
     * @param listener
     * @param negativeBtnText NegativeButton显示的文字
     * @param positiveBtnText PositiveButton显示的文字
     */
    public void setOnButtonClickListener(OnButtonClickListener listener,
                                         String negativeBtnText, String positiveBtnText) {
        if (listener == null)
            return;
        mListener = listener;

        mPositiveBtn.setText(positiveBtnText);
        mPositiveBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mListener.onPositiveButtonClick(view);
            }
        });
        mNegativeBtn.setText(negativeBtnText);
        mNegativeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mListener.onNegativeButtonClick(view);
            }
        });
    }

    /**
     * 设置监听器
     *
     * @param listener             监听器
     * @param negativeBtnTextResId NegativeButton显示的文字ResID
     * @param positiveBtnTextResId PositiveButton显示的文字ResID
     */
    public void setOnButtonClickListener(OnButtonClickListener listener,
                                         int negativeBtnTextResId, int positiveBtnTextResId) {
        setOnButtonClickListener(listener, mContext.getResources().getString(negativeBtnTextResId),
                mContext.getResources().getString(positiveBtnTextResId));
    }

    private OnPositiveButtonClickListener mPositiveListener;

    public void setOnPositiveButtonClickListener(OnPositiveButtonClickListener listener, String positiveBtnText) {
        if (listener == null)
            return;
        mPositiveListener = listener;
        mPositiveBtn.setText(positiveBtnText);
        mPositiveBtn.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_dialog_bg_single_btn_circle));
        mPositiveBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mPositiveListener.onPositiveButtonClick(view);
            }
        });
    }

    public void setOnPositiveButtonClickListener(OnPositiveButtonClickListener listener, int negativeBtnTextResId) {
        setOnPositiveButtonClickListener(listener, mContext.getResources().getString(negativeBtnTextResId));
    }

    private OnNegativeButtonClickListener mNegativeListener;

    public void setOnNegativeButtonClickListener(OnNegativeButtonClickListener listener, String negativeBtnText) {
        if (listener == null)
            return;
        mNegativeListener = listener;
        mNegativeBtn.setVisibility(View.VISIBLE);
        mNegativeBtn.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_dialog_bg_single_btn_circle));
        mNegativeBtn.setText(negativeBtnText);
        mNegativeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mNegativeListener.onNegativeButtonClick(view);
            }
        });
    }

    public void setOnNegativeButtonClickListener(OnNegativeButtonClickListener listener, int negativeBtnTextResId) {
        setOnNegativeButtonClickListener(listener, mContext.getResources().getString(negativeBtnTextResId));
    }

    //设置内容居左
    public void setContentLeft(){
        mContentTextView.setGravity(Gravity.LEFT);
    }


    /**
     * 显示简单的对话框
     *
     * @param content 内容
     */
    public static void showSimpleDialog(Context context, String content) {
        final PsiDialog dialog = new PsiDialog(context, content);
        dialog.setOnPositiveButtonClickListener(
                new OnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(View view) {
                        dialog.dismiss();
                    }
                }, "确认");
        dialog.show();
    }


    /**
     * 显示错误对话框
     *
     * @param context
     * @param strResId
     */
    public static void showErrorDialog(Context context, int strResId) {
        final PsiDialog dialog = new PsiDialog(context, strResId);
        dialog.setOnPositiveButtonClickListener(
                new OnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(View view) {
                        dialog.dismiss();
                    }
                }, "确认");
        dialog.show();
    }

}
