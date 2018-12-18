package demofon.example.com.opener.domofon;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.openid.appauth.AuthState;

import java.util.List;

import demofon.example.com.opener.R;
import demofon.example.com.opener.constants.Constants;
import demofon.example.com.opener.interfaces.CallbackDomofonOpen;
import demofon.example.com.opener.interfaces.CallbackToken;
import demofon.example.com.opener.login.AuthStateLogin;
import demofon.example.com.opener.login.AuthToken;

public class DomofonAdapter extends RecyclerView.Adapter<DomofonAdapter.ViewHolder> {
    private List<DomofonList> domofonLists;
    private Context context;
    private Activity activity;

    public DomofonAdapter(List<DomofonList> domofonLists, Context context, Activity activity) {
        this.domofonLists = domofonLists;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.domofon_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final DomofonList domofonList = domofonLists.get(position);
        writeDomofonPreference(
                String.valueOf(position),
                domofonList.getDomofon(),
                domofonList.getDomofon_id()
        );
        holder.address.setText(domofonList.getDomofon());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTheDoor(domofonList.getDomofon_id(), holder);

            }
        });
    }

    private void writeDomofonPreference(String position, String domofon, String domofonId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.DOMOFON_PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
        sharedPreferences.edit()
                .putString(position + "address", domofon)
                .putString(position + "domofon", domofonId)
                .putInt("size", getItemCount())
                .apply();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView address;
        public ImageView lockImg;
        public ProgressBar progressBar;
        public View layout;

        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView;
            address = (TextView) itemView.findViewById(R.id.domofonTxtAdress);
            lockImg = (ImageView) itemView.findViewById(R.id.domofonImgLock);
            progressBar = (ProgressBar) itemView.findViewById(R.id.domofonProgressBar);
        }
    }

    @Override
    public int getItemCount() {
        return domofonLists.size();
    }

    private void openTheDoor(final String id, final ViewHolder holder) {
        setProgress(true, holder);
        DomofonRequest.openDomofon(context, id, new CallbackDomofonOpen() {
            @Override
            public void onSuccess() {
                setProgress(false, holder);
                Toast.makeText(context, context.getString(R.string.domofon_adapter_open), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRefresh(AuthState authState) {
                refreshToken(authState, id, holder);
            }

            @Override
            public void onServerError() {
                setProgress(false, holder);
                Toast.makeText(context, context.getString(R.string.domofon_adapter_server), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                setProgress(false, holder);
                Toast.makeText(context, context.getString(R.string.error) + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshToken(AuthState authState, final String id, final ViewHolder holder) {
        AuthToken.refreshToken(context, authState, new CallbackToken() {
            @Override
            public void onSuccess(AuthState authState) {
                AuthStateLogin stateLogin = new AuthStateLogin(context);
                stateLogin.writeAuthState(authState);
                openTheDoor(id, holder);
            }

            @Override
            public void onError(String error) {
                setProgress(false, holder);
                Toast.makeText(context, context.getString(R.string.error_token) + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setProgress(boolean progress, ViewHolder holder) {
        if (progress) {
            holder.progressBar.setVisibility(View.VISIBLE);
        } else holder.progressBar.setVisibility(View.INVISIBLE);
    }
}
