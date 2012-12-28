package com.dsatab.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dsatab.R;
import com.dsatab.activity.MainActivity;

public class PortraitViewDialog extends AlertDialog implements DialogInterface.OnClickListener {

	private MainActivity main;

	public PortraitViewDialog(MainActivity context) {
		super(context);
		this.main = context;
		init();
	}

	protected MainActivity getMain() {
		return main;
	}

	private void init() {
		setTitle(main.getHero().getName());

		setCanceledOnTouchOutside(true);

		RelativeLayout popupcontent = (RelativeLayout) LayoutInflater.from(getContext()).inflate(
				R.layout.popup_portrait_view, null, false);
		popupcontent.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		setView(popupcontent);

		final ImageView image = (ImageView) popupcontent.findViewById(R.id.portrait_view);
		Bitmap drawable = main.getHero().getPortrait();
		if (drawable != null)
			image.setImageBitmap(drawable);
		else
			image.setImageResource(R.drawable.profile_picture);

		setButton(BUTTON_NEUTRAL, getContext().getString(R.string.label_ok), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.DialogInterface.OnClickListener#onClick(android.content
	 * .DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == BUTTON_NEUTRAL)
			this.dismiss();

	}

}
