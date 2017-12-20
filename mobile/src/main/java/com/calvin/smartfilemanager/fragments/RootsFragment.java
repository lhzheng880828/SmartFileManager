/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.calvin.smartfilemanager.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.calvin.smartfilemanager.BaseActivity;
import com.calvin.smartfilemanager.BaseActivity.State;
import com.calvin.smartfilemanager.DocumentsApplication;
import com.calvin.smartfilemanager.R;
import com.calvin.smartfilemanager.adapter.RootsExpandableAdapter;
import com.calvin.smartfilemanager.loader.RootsLoader;
import com.calvin.smartfilemanager.misc.AnalyticsManager;
import com.calvin.smartfilemanager.misc.CrashReportingManager;
import com.calvin.smartfilemanager.misc.RootsCache;
import com.calvin.smartfilemanager.model.DocumentInfo;
import com.calvin.smartfilemanager.model.GroupInfo;
import com.calvin.smartfilemanager.model.RootInfo;
import com.calvin.smartfilemanager.provider.ExplorerProvider;
import com.calvin.smartfilemanager.provider.ExternalStorageProvider;
import com.calvin.smartfilemanager.setting.SettingsActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import static com.calvin.smartfilemanager.BaseActivity.State.ACTION_BROWSE;

/**
 * Display list of known storage backend roots.
 */
public class RootsFragment extends Fragment {

    private int group_size = 0;
    private ArrayList<Long> expandedIds = new ArrayList<>();

    private ExpandableListView mList;
    private RootsExpandableAdapter mAdapter;

    private LoaderCallbacks<Collection<RootInfo>> mCallbacks;

    private static final String EXTRA_INCLUDE_APPS = "includeApps";

    public static void show(FragmentManager fm, Intent includeApps) {
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_INCLUDE_APPS, includeApps);

        final RootsFragment fragment = new RootsFragment();
        fragment.setArguments(args);

        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container_roots, fragment);
        ft.commitAllowingStateLoss();
    }

    public static RootsFragment get(FragmentManager fm) {
        return (RootsFragment) fm.findFragmentById(R.id.container_roots);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context = inflater.getContext();

        final View view = inflater.inflate(R.layout.fragment_roots, container, false);
        mList = (ExpandableListView) view.findViewById(android.R.id.list);
        mList.setOnChildClickListener(mItemListener);
        mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Context context = getActivity();
        final RootsCache roots = DocumentsApplication.getRootsCache(context);
        final State state = ((BaseActivity) context).getDisplayState();

        mCallbacks = new LoaderCallbacks<Collection<RootInfo>>() {
            @Override
            public Loader<Collection<RootInfo>> onCreateLoader(int id, Bundle args) {
                return new RootsLoader(context, roots, state);
            }

            @Override
            public void onLoadFinished(
                    Loader<Collection<RootInfo>> loader, Collection<RootInfo> result) {
                if (!isAdded()) return;

                final Intent includeApps = getArguments().getParcelable(EXTRA_INCLUDE_APPS);

                mAdapter = new RootsExpandableAdapter(context, result, includeApps);
                mList.setAdapter(mAdapter);

                onCurrentRootChanged();
            }

            @Override
            public void onLoaderReset(Loader<Collection<RootInfo>> loader) {
                mAdapter = null;
                mList.setAdapter((ExpandableListAdapter) null);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        final Context context = getActivity();
        final State state = ((BaseActivity) context).getDisplayState();
        state.showAdvanced = state.forceAdvanced
                | SettingsActivity.getDisplayAdvancedDevices(context);
        state.rootMode = SettingsActivity.getRootMode(getActivity());

        if (state.action == ACTION_BROWSE) {
            mList.setOnItemLongClickListener(mItemLongClickListener);
        } else {
            mList.setOnItemLongClickListener(null);
            mList.setLongClickable(false);
        }

        getLoaderManager().restartLoader(2, null, mCallbacks);
    }

    public void onDisplayStateChanged() {
        final Context context = getActivity();
        final State state = ((BaseActivity) context).getDisplayState();

        if (state.action == State.ACTION_GET_CONTENT) {
            mList.setOnItemLongClickListener(mItemLongClickListener);
        } else {
            mList.setOnItemLongClickListener(null);
            mList.setLongClickable(false);
        }

        getLoaderManager().restartLoader(2, null, mCallbacks);
    }

    public void onCurrentRootChanged() {
        if (mAdapter == null || mList == null) return;

        final RootInfo root = ((BaseActivity) getActivity()).getCurrentRoot();
        for (int i = 0; i < mAdapter.getGroupCount(); i++) {
            for (int j = 0; j < mAdapter.getChildrenCount(i); j++) {
                final Object item = mAdapter.getChild(i,j);
                if (item instanceof RootItem) {
                    final RootInfo testRoot = ((RootItem) item).root;
                    if (Objects.equals(testRoot, root)) {
                        try {
                            long id = ExpandableListView.getPackedPositionForChild(i, j);
                            int index = mList.getFlatListPosition(id);
                            //mList.setSelection(index);
                            mList.setItemChecked(index, true);
                        } catch (Exception e){
                            CrashReportingManager.logException(e);
                        }

                        return;
                    }
                }
            }
        }
    }

    private void showAppDetails(ResolveInfo ri) {
        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", ri.activityInfo.packageName, null));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(intent);
    }


    private ExpandableListView.OnChildClickListener mItemListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View view, int groupPosition,
                                    int childPosition, long id) {
            final BaseActivity activity = BaseActivity.get(RootsFragment.this);
            final Item item = (Item) mAdapter.getChild(groupPosition, childPosition);
            if (item instanceof RootItem) {
                int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
                parent.setItemChecked(index, true);
                activity.onRootPicked(((RootItem) item).root, true);
                Bundle params = new Bundle();
                params.putString("type", ((RootItem) item).root.title);
                AnalyticsManager.logEvent("navigate", ((RootItem) item).root, params);
            } else if (item instanceof AppItem) {
                activity.onAppPicked(((AppItem) item).info);
            } else {
                throw new IllegalStateException("Unknown root: " + item);
            }
            return false;
        }
    };


    private AdapterView.OnItemLongClickListener mItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            int itemType = ExpandableListView.getPackedPositionType(id);
            int childPosition;
            int groupPosition;

            if ( itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                childPosition = ExpandableListView.getPackedPositionChild(id);
                groupPosition = ExpandableListView.getPackedPositionGroup(id);
                final Item item = (Item) mAdapter.getChild(groupPosition, childPosition);
                if (item instanceof AppItem) {
                    showAppDetails(((AppItem) item).info);
                    return true;
                } else if (item instanceof BookmarkItem) {
                    removeBookark((BookmarkItem)item);
                    return true;
                }  else {
                    return false;
                }

            } else if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                groupPosition = ExpandableListView.getPackedPositionGroup(id);
                return false;

            } else {
                return false;
            }
        }
    };

    private ExpandableListView.OnGroupExpandListener mOnGroupExpandListener = new ExpandableListView.OnGroupExpandListener() {
        @Override
        public void onGroupExpand(int i) {
            expandedIds.add(mAdapter.getGroupId(i));
        }
    };

    private ExpandableListView.OnGroupCollapseListener mOnGroupCollapseListener = new ExpandableListView.OnGroupCollapseListener() {
        @Override
        public void onGroupCollapse(int i) {
            expandedIds.remove(mAdapter.getGroupId(i));
        }
    };

    private void removeBookark(final BookmarkItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Remove bookmark?")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int did) {
                        dialog.dismiss();
                        int rows = getActivity().getContentResolver().delete(ExplorerProvider.buildBookmark(),
                                ExplorerProvider.BookmarkColumns.PATH + " = ? AND " +
                                        ExplorerProvider.BookmarkColumns.TITLE + " = ? ",
                                new String[]{item.root.path, item.root.title}
                        );
                        if (rows > 0) {
                            ((BaseActivity) getActivity()).showInfo("Bookmark removed");

                            RootsCache.updateRoots(getActivity(), ExternalStorageProvider.AUTHORITY);
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int did) {
                dialog.dismiss();
            }
        });
        DialogFragment.showThemedDialog(builder);
    }

    public static abstract class Item {
        private final int mLayoutId;

        public Item(int layoutId) {
            mLayoutId = layoutId;
        }

        public View getView(View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(mLayoutId, parent, false);
            }
            bindView(convertView);
            return convertView;
        }

        public abstract void bindView(View convertView);
    }

    public static class RootItem extends Item {
        public final RootInfo root;

        private final int color;

        public RootItem(RootInfo root) {
            super(R.layout.item_root);
            this.root = root;
            this.color = SettingsActivity.getPrimaryColor();
        }

        public RootItem(RootInfo root, int color) {
            super(R.layout.item_root);
            this.root = root;
            this.color = color;
        }

        @Override
        public void bindView(View convertView) {
            final ImageView icon = (ImageView) convertView.findViewById(android.R.id.icon);
            final TextView title = (TextView) convertView.findViewById(android.R.id.title);
            final TextView summary = (TextView) convertView.findViewById(android.R.id.summary);

            final Context context = convertView.getContext();
            icon.setImageDrawable(root.loadDrawerIcon(context));
            title.setText(root.title);

            // Show available space if no summary
            String summaryText = root.summary;
            if (TextUtils.isEmpty(summaryText) && root.availableBytes >= 0) {
                summaryText = context.getString(R.string.root_available_bytes,
                        Formatter.formatFileSize(context, root.availableBytes));
            }

            summary.setText(summaryText);
            summary.setVisibility(TextUtils.isEmpty(summaryText) ? View.GONE : View.VISIBLE);
        }
    }

    public static class SpacerItem extends Item {
        public SpacerItem() {
            super(R.layout.item_root_spacer);
        }

        @Override
        public void bindView(View convertView) {
            // Nothing to bind
        }
    }

    public static class AppItem extends Item {
        public final ResolveInfo info;

        public AppItem(ResolveInfo info) {
            super(R.layout.item_root);
            this.info = info;
        }

        @Override
        public void bindView(View convertView) {
            final ImageView icon = (ImageView) convertView.findViewById(android.R.id.icon);
            final TextView title = (TextView) convertView.findViewById(android.R.id.title);
            final TextView summary = (TextView) convertView.findViewById(android.R.id.summary);

            final PackageManager pm = convertView.getContext().getPackageManager();
            icon.setImageDrawable(info.loadIcon(pm));
            title.setText(info.loadLabel(pm));

            // TODO: match existing summary behavior from disambig dialog
            summary.setVisibility(View.GONE);
        }
    }

    public static class GroupItem {
        public final String mLabel;
        private final int mLayoutId;

        public GroupItem(GroupInfo groupInfo) {
            mLabel = groupInfo.label;
            mLayoutId = R.layout.item_root_header;
        }

        public View getView(View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(mLayoutId, parent, false);
            }
            bindView(convertView);
            return convertView;
        }

        public void bindView(View convertView) {
            final TextView title = (TextView) convertView.findViewById(android.R.id.title);
            title.setText(mLabel);
        }

    }

    public static class BookmarkItem extends RootItem {
        public BookmarkItem(RootInfo root) {
            super(root, 0);
        }
    }


    public static class RootComparator implements Comparator<RootInfo> {
        @Override
        public int compare(RootInfo lhs, RootInfo rhs) {
            final int score = DocumentInfo.compareToIgnoreCaseNullable(lhs.title, rhs.title);
            if (score != 0) {
                return score;
            } else {
                return DocumentInfo.compareToIgnoreCaseNullable(lhs.summary, rhs.summary);
            }
        }
    }
}
