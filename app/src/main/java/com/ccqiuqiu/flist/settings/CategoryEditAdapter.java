package com.ccqiuqiu.flist.settings;


import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ccqiuqiu.flist.App;
import com.ccqiuqiu.flist.R;
import com.ccqiuqiu.flist.model.Category;
import com.ccqiuqiu.flist.model.Note;
import com.ccqiuqiu.flist.model.Todo;
import com.ccqiuqiu.flist.utils.DbUtils;
import com.ccqiuqiu.flist.utils.InputUtils;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.util.List;


public class CategoryEditAdapter extends
        RecyclerView.Adapter<CategoryEditAdapter.MyViewHolder> {

    private List<Category> categories;
    private Fragment mFragment;
    private SettingsActivity mSettingsActivity;
    private int mflg;
    private SharedPreferences mySharedPreferences;

    public List<Category> getCategories() {
        return categories;
    }

    public CategoryEditAdapter(int flg,Fragment fragment) {
        mySharedPreferences = fragment.getContext().getSharedPreferences("config", fragment.getContext().MODE_PRIVATE);
        mflg = flg;
        mFragment = fragment;
        mSettingsActivity = (SettingsActivity) fragment.getActivity();
        DbManager db = DbUtils.getDbManager();
        try {
            categories = db.selector(Category.class).where("flg","=",flg).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
    @Override
    public int getItemCount() {
        return categories.size();
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.list_item_category_edit,parent,false);;
        return new MyViewHolder(v);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Category category = categories.get(position);
        holder.mTitle.setText(category.getName());
        holder.mTitle.setBaseColor(App.colorPrimary);
        if(category.getAllowDel()){
            holder.mDel.setVisibility(View.VISIBLE);
            holder.mDel.setEnabled(true);
        }else{
            //holder.mDel.setVisibility(View.GONE);
            holder.mDel.setEnabled(false);
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            holder.mDel.setBackgroundResource(R.drawable.btn_selector_del);
            holder.mEdit.setBackgroundResource(R.drawable.btn_selector_edit);
        }
        holder.mTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    saveCategory(v,holder,position);
                }
            }
        });
        holder.mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCategory(v, holder, position);
            }
        });
        holder.mDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delCateGory(v, holder, position);
            }
        });
    }

    private void delCateGory(final View v, final MyViewHolder holder, final int position) {
        new MaterialDialog.Builder(v.getContext())
                .title(R.string.delete)
                .content(R.string.del_ca_content)
                .positiveText(R.string.confirm)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        Category category = categories.get(holder.getLayoutPosition());
                        DbManager db = DbUtils.getDbManager();
                        try {
                            if (mflg == 0) {
                                db.delete(Todo.class, WhereBuilder.b("categoryId", "=", category.getId()));
                                //如果这个分类是保存的分类，那么删除它  以免下次进入软件分类出错
                                int mTodoCateGoryId = mySharedPreferences.getInt("sett_category_0_id", 1);
                                if (mTodoCateGoryId == category.getId()) {
                                    mySharedPreferences.edit().remove("sett_category_0_id").commit();
                                }
                            } else {
                                db.delete(Note.class, WhereBuilder.b("categoryId", "=", category.getId()));
                                int mNoteCateGoryId = mySharedPreferences.getInt("sett_category_1_id", 2);
                                if (mNoteCateGoryId == category.getId()) {
                                    mySharedPreferences.edit().remove("sett_category_1_id").commit();
                                }
                            }

                            mSettingsActivity.mDelCategoryIds.add(category.getId());
                            mSettingsActivity.mySetResult();

                            db.delete(category);
                            categories.remove(holder.getLayoutPosition());
                            notifyItemRemoved(holder.getLayoutPosition());
                            //
                            Toast.makeText(v.getContext(),
                                    v.getContext().getResources().getString(R.string.del_success), Toast.LENGTH_SHORT)
                                    .show();
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }

    private void saveCategory(View v,MyViewHolder holder, int position) {
        try {
            String name = holder.mTitle.getText().toString().trim();
            if(TextUtils.isEmpty(name)){
                holder.mTitle.setError(v.getResources().getString(R.string.err_null));
                return;
            }
            if(name.length() > 16){
                holder.mTitle.setError(v.getResources().getString(R.string.err_length));
                return;
            }
            Category category = categories.get(position);
            if(category.getName().equals(name)){
                return;
            }
            DbManager db = DbUtils.getDbManager();
            category.setName(name);
            db.update(category);
            notifyItemChanged(position);

            InputUtils.HideKeyboard(holder.mTitle);

            Toast.makeText(v.getContext(),
                    v.getContext().getResources().getString(R.string.edit_success),Toast.LENGTH_SHORT)
                    .show();
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private MaterialEditText mTitle;
        private Button mEdit;
        private Button mDel;


        public MyViewHolder(View v) {
            super(v);
            mTitle = (MaterialEditText) v.findViewById(R.id.tv_title);
            mEdit = (Button) v.findViewById(R.id.btn_edit);
            mDel = (Button) v.findViewById(R.id.btn_del);
        }
    }
}
