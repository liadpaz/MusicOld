package com.liadpaz.amp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liadpaz.amp.R;
import com.liadpaz.amp.databinding.ItemArtistBinding;
import com.liadpaz.amp.viewmodels.Artist;

import java.util.List;

public class ArtistsListAdapter extends ArrayAdapter<Artist> {

    public ArtistsListAdapter(@NonNull Context context, @NonNull List<Artist> artists) {
        super(context, R.layout.item_artist, artists);
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ItemArtistBinding binding;
        if (convertView == null) {
            binding = ItemArtistBinding.inflate(LayoutInflater.from(getContext()), parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (ItemArtistBinding)convertView.getTag();
        }

        Artist artist = getItem(position);

        binding.tvArtistName.setText(artist.name);
        binding.tvArtistCount.setText(String.valueOf(artist.songs.size()));

        return convertView;
    }
}
