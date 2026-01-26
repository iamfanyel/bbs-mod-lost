package mchorse.bbs_mod.ui.model;

import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.cubic.ModelInstance;
import mchorse.bbs_mod.cubic.model.ModelConfig;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.settings.values.core.ValueList;
import mchorse.bbs_mod.settings.values.core.ValueString;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIIcon;
import mchorse.bbs_mod.ui.framework.elements.context.UIContextMenu;
import mchorse.bbs_mod.ui.framework.elements.input.list.UISearchList;
import mchorse.bbs_mod.ui.framework.elements.input.list.UIStringList;
import mchorse.bbs_mod.ui.framework.elements.utils.UILabel;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import mchorse.bbs_mod.utils.colors.Colors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class UIModelItemsSection extends UIModelSection
{
    private UIStringList mainList;
    private UIStringList offList;

    public UIModelItemsSection(UIModelPanel editor)
    {
        super(editor);

        this.title.label = UIKeys.MODELS_ITEMS;

        this.mainList = new UIStringList(null);
        this.mainList.background = 0x88000000;
        this.offList = new UIStringList(null);
        this.offList.background = 0x88000000;

        UIElement mainCol = this.createListColumn(UIKeys.MODELS_ITEMS_MAIN, this.mainList, () -> this.config.itemsMain);
        UIElement offCol = this.createListColumn(UIKeys.MODELS_ITEMS_OFF, this.offList, () -> this.config.itemsOff);

        this.fields.add(UI.row(mainCol, offCol));
        this.fields.h(120); // Fixed height for lists
    }

    @Override
    public IKey getTitle()
    {
        return UIKeys.MODELS_ITEMS;
    }

    private void updateLists()
    {
        java.util.List<String> main = new java.util.ArrayList<>();
        java.util.List<String> off = new java.util.ArrayList<>();

        for (ValueString value : this.config.itemsMain.getList())
        {
            main.add(value.get());
        }

        for (ValueString value : this.config.itemsOff.getList())
        {
            off.add(value.get());
        }

        this.mainList.setList(main);
        this.offList.setList(off);
    }

    private UIElement createListColumn(IKey label, UIStringList list, java.util.function.Supplier<ValueList<ValueString>> valueSupplier)
    {
        UIElement column = new UIElement();
        column.column(5).stretch().vertical();

        UILabel labelElement = new UILabel(label);
        
        UIIcon add = new UIIcon(Icons.ADD, (b) ->
        {
            if (this.config == null)
            {
                return;
            }

            ModelInstance model = BBSModClient.getModels().getModel(this.config.getId());

            if (model == null)
            {
                return;
            }

            List<String> groups = new ArrayList<>(model.getModel().getAllGroupKeys());
            Collections.sort(groups);
            groups.add(0, "<none>");

            UIStringListContextMenu menu = new UIStringListContextMenu(groups, () -> list.getList(), (group) ->
            {
                ValueList<ValueString> valueList = valueSupplier.get();

                if (group.equals("<none>"))
                {
                    valueList.getAllTyped().clear();
                }
                else
                {
                    boolean exists = false;
                    int index = -1;
                    int i = 0;

                    for (ValueString value : valueList.getList())
                    {
                        if (value.get().equals(group))
                        {
                            exists = true;
                            index = i;
                            break;
                        }

                        i++;
                    }

                    if (exists)
                    {
                        valueList.getAllTyped().remove(index);
                    }
                    else
                    {
                        valueList.add(new ValueString(String.valueOf(valueList.getList().size()), group));
                    }
                }

                this.updateLists();
                this.editor.dirty();
            });

            this.getContext().replaceContextMenu(menu);
        });
        
        UIElement icons = UI.row(add);
        icons.h(20);

        column.add(labelElement, list, icons);

        return column;
    }

    @Override
    public void deselect()
    {
        this.mainList.deselect();
        this.offList.deselect();
    }

    @Override
    public void setConfig(ModelConfig config)
    {
        super.setConfig(config);

        if (config != null)
        {
            this.updateLists();
        }
    }

    public static class UIStringListContextMenu extends UIContextMenu
    {
        public UISearchList<String> list;

        public UIStringListContextMenu(List<String> groups, java.util.function.Supplier<Collection<String>> selected, Consumer<String> callback)
        {
            this.list = new UISearchList<>(new UIStringList((l) ->
            {
                if (l.get(0) != null)
                {
                    callback.accept(l.get(0));
                }
            })
            {
                @Override
                protected void renderElementPart(UIContext context, String element, int i, int x, int y, boolean hover, boolean selectedState)
                {
                    if (selected.get().contains(element))
                    {
                        context.batcher.box(x, y, x + this.area.w, y + this.scroll.scrollItemSize, Colors.A50 | BBSSettings.primaryColor.get());
                    }

                    super.renderElementPart(context, element, i, x, y, hover, selectedState);
                }
            });
            this.list.list.setList(groups);
            this.list.list.background = 0xaa000000;
            this.list.relative(this).xy(5, 5).w(1F, -10).h(1F, -10);
            this.list.search.placeholder(UIKeys.POSE_CONTEXT_NAME);

            this.add(this.list);
        }

        @Override
        public boolean isEmpty()
        {
            return this.list.list.getList().isEmpty();
        }

        @Override
        public void setMouse(UIContext context)
        {
            this.xy(context.mouseX(), context.mouseY()).w(120).h(200).bounds(context.menu.overlay, 5);
        }
    }
}
