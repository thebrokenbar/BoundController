/*
 * *
 *  * Copyright 2017 Grzegorz Wierzchanowski
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package pl.brokenpipe.boundcontroller;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;

public abstract class BoundController<V extends ViewDataBinding> extends Controller {

    private static final int FADE_ANIMATION_DURATION = 100;
    private V binding;
    private OnViewResult onViewResultListener;

    public BoundController() {
    }

    public BoundController(Bundle args) {
        super(args);
    }

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        int layoutId = getLayoutId();
        
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false);
        onViewBound(binding);
        binding.executePendingBindings();
        return binding.getRoot();
    }

    private int getLayoutId() {
        Layout layoutAnnotation = this.getClass().getAnnotation(Layout.class);
        if (layoutAnnotation == null || layoutAnnotation.value() == 0) {
            throw new UnsupportedOperationException(
                "Cannot create view without layout resource annotated");
        }
        return layoutAnnotation.value();
    }

    protected void onViewBound(@NonNull V binding) {
    }

    protected void onViewUnbound(@NonNull V binding) {
    }

    @Override
    protected void onDetach(@NonNull View view) {
        super.onDetach(view);
        onViewUnbound(binding);
        binding.unbind();
    }

    public void setOnViewResultListener(OnViewResult onViewResult) {
        onViewResultListener = onViewResult;
    }

    protected void returnResult(int resultCode, Bundle params) {
        getRouter().popController(this);
        onViewResultListener.onResult(resultCode, params);
    }

    protected void showView(BoundController view) {
        view.setTargetController(this);
        getRouter().pushController(RouterTransaction.with(view)
            .pushChangeHandler(new FadeChangeHandler(FADE_ANIMATION_DURATION))
            .popChangeHandler(new FadeChangeHandler(FADE_ANIMATION_DURATION))
            .tag(view.getClass().getSimpleName()));
    }

    protected void showViewForResult(BoundController view, OnViewResult onViewResult) {
        view.setTargetController(this);
        showView(view);
    }

    public AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }
}
