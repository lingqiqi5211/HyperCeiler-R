/*
 * This file is part of HyperCeiler.

 * HyperCeiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2025 HyperCeiler Contributions
 */
package com.sevtinge.hyperceiler.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fan.appcompat.app.Fragment;

public class DetailFragment extends Fragment {

    private static final String FRAGMENT_NAME_KEY = "FragmentName";

    private String mFragmentName;
    private View mEmptyView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeRes(com.sevtinge.hyperceiler.ui.R.style.NavigatorSecondaryContentTheme);
    }

    @Override
    public View onInflateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(com.sevtinge.hyperceiler.ui.R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewInflated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewInflated(view, savedInstanceState);
        mEmptyView = view.findViewById(com.sevtinge.hyperceiler.ui.R.id.empty);
        hideActionBarIfNeeded();
    }

    @Override
    public void onUpdateArguments(Bundle args) {
        super.onUpdateArguments(args);
        
        if (args != null) {
            mFragmentName = args.getString(FRAGMENT_NAME_KEY);
            loadFragmentIfValid(args);
        } else {
            showEmptyView();
        }
    }

    private void hideActionBarIfNeeded() {
        if (getActionBar() != null) {
            getActionBar().hide();
        }
    }

    private void loadFragmentIfValid(Bundle args) {
        if (!TextUtils.isEmpty(mFragmentName)) {
            loadFragment(args);
            hideEmptyView();
        } else {
            showEmptyView();
        }
    }

    private void loadFragment(Bundle args) {
        try {
            androidx.fragment.app.Fragment fragment = androidx.fragment.app.Fragment.instantiate(
                requireContext(), 
                mFragmentName, 
                args
            );
            
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(com.sevtinge.hyperceiler.ui.R.id.frame_content, fragment)
                    .commit();
        } catch (Exception e) {
            // Handle fragment instantiation errors gracefully
            e.printStackTrace();
            showEmptyView();
        }
    }

    private void showEmptyView() {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyView() {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.INVISIBLE);
        }
    }
}
