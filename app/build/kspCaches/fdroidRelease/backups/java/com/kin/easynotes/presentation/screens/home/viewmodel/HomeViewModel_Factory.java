package com.kin.easynotes.presentation.screens.home.viewmodel;

import android.content.Context;
import com.kin.easynotes.domain.usecase.NoteUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<NoteUseCase> noteUseCaseProvider;

  private final Provider<Context> contextProvider;

  private HomeViewModel_Factory(Provider<NoteUseCase> noteUseCaseProvider,
      Provider<Context> contextProvider) {
    this.noteUseCaseProvider = noteUseCaseProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(noteUseCaseProvider.get(), contextProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<NoteUseCase> noteUseCaseProvider,
      Provider<Context> contextProvider) {
    return new HomeViewModel_Factory(noteUseCaseProvider, contextProvider);
  }

  public static HomeViewModel newInstance(NoteUseCase noteUseCase, Context context) {
    return new HomeViewModel(noteUseCase, context);
  }
}
