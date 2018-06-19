package com.frank.lib_picturepicker.picturepicker.mvp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.frank.lib_picturepicker.picturepicker.data.PictureFolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description:
 */
public interface PicturePickerContract {

    interface IView {

        /**
         * 显示选中的图片文件夹
         *
         * @param folderName 文件夹的名称
         * @param uris       文件夹中的数据
         */
        void displayPictures(String folderName, List<String> uris);

        /**
         * 展示消息通知
         *
         * @param msg
         */
        void showMsg(String msg);

        /**
         * 更新标题文本
         */
        void updateTextContent(int curPicked, int total);

        /**
         * 更新文本的可见性(选中图片大于 0 时, 即为可见)
         */
        void updateTextViewVisibility(boolean isVisible);
    }

    interface IPresenter {

        /**
         * 绑定 View
         */
        void attach(IView view);

        /**
         * 配置用户已经选中的图片集合
         *
         * @param userPicked 用户已经选中的图片集合
         */
        void setupUserPickedSet(ArrayList<String> userPicked);

        /**
         * 配置阈值
         */
        void setupThreshold(int threshold);

        /**
         * 初始化 Model 的数据
         */
        void initData(Context context);

        /**
         * 获取需要展示的图片
         *
         * @param position
         */
        void fetchDisplayPictures(int position);

        /**
         * 获取所有图片文件夹
         */
        ArrayList<PictureFolder> fetchAllPictureFolders();

        /**
         * 获取用户选中的所有图片
         */
        ArrayList<String> fetchUserPickedSet();

        /**
         * 处理图片被选中了
         */
        boolean performPicturePicked(String imagePath);

        /**
         * 处理图片被移除了
         */
        void performPictureRemoved(String imagePath);

    }

    interface IModel {

        /**
         * 设置用户已经选中的图片集合
         */
        void setUserPickedSet(@NonNull ArrayList<String> userPicked);

        /**
         * 获取系统图片
         */
        void getSystemPictures(Context context, final ModelInitializeCallback listener);

        /**
         * 设置图片选择的阈值
         */
        void setThreshold(int threshold);

        /**
         * 获取图片选择的阈值
         */
        int getThreshold();

        /**
         * 获取当前需要显示的文件模型
         */
        PictureFolder getPictureFolderAt(int index);

        /**
         * 获取所有的图片文件夹
         */
        ArrayList<PictureFolder> getAllPictureFolders();

        /**
         * 获取用户选中的图片
         *
         * @return
         */
        ArrayList<String> getUserPickedSet();

        /**
         * 添加用户选中的图片
         */
        void addPickedPicture(String imagePath);

        /**
         * 移除用户选中的图片
         */
        void removePickedPicture(String imagePath);
    }

    interface ModelInitializeCallback {
        void onComplete(List<PictureFolder> pictureFolders);

        void onFailed(Throwable throwable);
    }
}