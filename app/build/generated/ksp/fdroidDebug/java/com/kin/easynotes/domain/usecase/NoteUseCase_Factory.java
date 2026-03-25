package com.kin.easynotes.domain.usecase;

import android.content.Context;
import com.kin.easynotes.data.repository.NoteRepositoryImpl;
import com.kin.easynotes.presentation.components.EncryptionHelper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;

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
public final class NoteUseCase_Factory implements Factory<NoteUseCase> {
  private final Provider<NoteRepositoryImpl> noteRepositoryProvider;

  private final Provider<CoroutineScope> coroutineScopeProvider;

  private final Provider<EncryptionHelper> encryptionHelperProvider;

  private final Provider<Context> contextProvider;

  private NoteUseCase_Factory(Provider<NoteRepositoryImpl> noteRepositoryProvider,
      Provider<CoroutineScope> coroutineScopeProvider,
      Provider<EncryptionHelper> encryptionHelperProvider, Provider<Context> contextProvider) {
    this.noteRepositoryProvider = noteRepositoryProvider;
    this.coroutineScopeProvider = coroutineScopeProvider;
    this.encryptionHelperProvider = encryptionHelperProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public NoteUseCase get() {
    return newInstance(noteRepositoryProvider.get(), coroutineScopeProvider.get(), encryptionHelperProvider.get(), contextProvider.get());
  }

  public static NoteUseCase_Factory create(Provider<NoteRepositoryImpl> noteRepositoryProvider,
      Provider<CoroutineScope> coroutineScopeProvider,
      Provider<EncryptionHelper> encryptionHelperProvider, Provider<Context> contextProvider) {
    return new NoteUseCase_Factory(noteRepositoryProvider, coroutineScopeProvider, encryptionHelperProvider, contextProvider);
  }

  public static NoteUseCase newInstance(NoteRepositoryImpl noteRepository,
      CoroutineScope coroutineScope, EncryptionHelper encryptionHelper, Context context) {
    return new NoteUseCase(noteRepository, coroutineScope, encryptionHelper, context);
  }
}
