package com.calvin.smartfilemanager.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.calvin.smartfilemanager.fragments.RootsFragment;
import com.calvin.smartfilemanager.model.RootInfo;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:17-12-20
 */

public  class RootsAdapter extends ArrayAdapter<RootsFragment.Item> {
    public RootsAdapter(Context context, Collection<RootInfo> roots, Intent includeApps) {
        super(context, 0);

        RootsFragment.RootItem recents = null;
        RootsFragment.RootItem images = null;
        RootsFragment.RootItem videos = null;
        RootsFragment.RootItem audio = null;
        RootsFragment.RootItem downloads = null;

        final List<RootInfo> clouds = Lists.newArrayList();
        final List<RootInfo> locals = Lists.newArrayList();

        for (RootInfo root : roots) {
            if (root.isRecents()) {
                recents = new RootsFragment.RootItem(root);
            } else if (root.isExternalStorage()) {
                locals.add(root);
            } else if (root.isDownloads()) {
                downloads = new RootsFragment.RootItem(root);
            } else if (root.isImages()) {
                images = new RootsFragment.RootItem(root);
            } else if (root.isVideos()) {
                videos = new RootsFragment.RootItem(root);
            } else if (root.isAudio()) {
                audio = new RootsFragment.RootItem(root);
            } else {
                clouds.add(root);
            }
        }

        final RootsFragment.RootComparator comp = new RootsFragment.RootComparator();
        Collections.sort(clouds, comp);
        Collections.sort(locals, comp);

        if (recents != null) add(recents);

        for (RootInfo cloud : clouds) {
            add(new RootsFragment.RootItem(cloud));
        }

        if (images != null) add(images);
        if (videos != null) add(videos);
        if (audio != null) add(audio);
        if (downloads != null) add(downloads);

        for (RootInfo local : locals) {
            add(new RootsFragment.RootItem(local));
        }

        if (includeApps != null) {
            final PackageManager pm = context.getPackageManager();
            final List<ResolveInfo> infos = pm.queryIntentActivities(
                    includeApps, PackageManager.MATCH_DEFAULT_ONLY);

            final List<RootsFragment.AppItem> apps = Lists.newArrayList();

            // Omit ourselves from the list
            for (ResolveInfo info : infos) {
                if (!context.getPackageName().equals(info.activityInfo.packageName)) {
                    apps.add(new RootsFragment.AppItem(info));
                }
            }

            if (apps.size() > 0) {
                add(new RootsFragment.SpacerItem());
                for (RootsFragment.Item item : apps) {
                    add(item);
                }
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final RootsFragment.Item item = getItem(position);
        return item.getView(convertView, parent);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != 1;
    }

    @Override
    public int getItemViewType(int position) {
        final RootsFragment.Item item = getItem(position);
        if (item instanceof RootsFragment.RootItem || item instanceof RootsFragment.AppItem) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}
