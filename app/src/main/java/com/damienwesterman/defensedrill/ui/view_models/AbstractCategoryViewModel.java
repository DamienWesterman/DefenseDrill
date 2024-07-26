/****************************\
 *      ________________      *
 *     /  _             \     *
 *     \   \ |\   _  \  /     *
 *      \  / | \ / \  \/      *
 *      /  \ | / | /  /\      *
 *     /  _/ |/  \__ /  \     *
 *     \________________/     *
 *                            *
 \****************************/

package com.damienwesterman.defensedrill.ui.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.damienwesterman.defensedrill.data.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.CreateNewEntityCallback;

import java.util.List;

/**
 * Abstract superclass for view models for any {@link AbstractCategoryEntity} subclass.
 * <br><br>
 * This view model is geared towards managing a list of all the AbstractCategoryEntities in the
 * database, with CRUD functionality.
 */
public abstract class AbstractCategoryViewModel extends AndroidViewModel {
    public AbstractCategoryViewModel(@NonNull Application application) {
        super(application);
    }

    public abstract LiveData<List<AbstractCategoryEntity>> getAbstractCategories();
    public abstract void populateAbstractCategories();
    public abstract void rePopulateAbstractCategories();
    public abstract void deleteAbstractCategory(AbstractCategoryEntity entity);
    public abstract void saveAbstractEntity(String name, String description, @NonNull CreateNewEntityCallback callback);
    public abstract void updateAbstractEntity(AbstractCategoryEntity entity, @NonNull CreateNewEntityCallback callback);
    @Nullable
    public abstract AbstractCategoryEntity findById(long id);
}
