package xyz.imxqd.clickclick.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.ahmadaghazadeh.editor.widget.CodeEditor;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.vimfung.luascriptcore.LuaContext;
import cn.vimfung.luascriptcore.LuaNativeUtil;
import cn.vimfung.luascriptcore.LuaScriptController;
import xyz.imxqd.clickclick.MyApp;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.luaframework.LuaEngine;

public class CodeEditorActivity extends AppCompatActivity {

    private static final String TAG = "code";

    @BindView(R.id.code_editor)
    CodeEditor codeEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_editor);
        ButterKnife.bind(this);
        StringBuilder code = new StringBuilder();
        code.append("--");
        code.append(LuaNativeUtil.luaVersion());
        code.append("\n");
        code.append("--api-version/0.0.1\n\n");
        codeEditor.setText(code.toString(), 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.code_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_run) {
            String code = codeEditor.getCode();
            LuaContext luaContext = LuaEngine.createContext();
            luaContext.onException(s -> {
                Log.e(TAG, s);
                MyApp.get().toastCenter(s);
            });
            LuaScriptController controller = LuaScriptController.create();
            luaContext.evalScript(code, controller);
            return true;

        } else if (item.getItemId() == R.id.action_save) {
            finish();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            return false;
        }
        return false;
    }
}
