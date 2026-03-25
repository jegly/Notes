package com.kin.easynotes.di;

import com.kin.easynotes.data.local.database.NoteDatabaseProvider;
import com.kin.easynotes.data.repository.NoteRepositoryImpl;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class ApplicationModule_ProvideNoteRepositoryFactory implements Factory<NoteRepositoryImpl> {
  private final Provider<NoteDatabaseProvider> noteDatabaseProvider;

  private ApplicationModule_ProvideNoteRepositoryFactory(
      Provider<NoteDatabaseProvider> noteDatabaseProvider) {
    this.noteDatabaseProvider = noteDatabaseProvider;
  }

  @Override
  public NoteRepositoryImpl get() {
    return provideNoteRepository(noteDatabaseProvider.get());
  }

  public static ApplicationModule_ProvideNoteRepositoryFactory create(
      Provider<NoteDatabaseProvider> noteDatabaseProvider) {
    return new ApplicationModule_ProvideNoteRepositoryFactory(noteDatabaseProvider);
  }

  public static NoteRepositoryImpl provideNoteRepository(
      NoteDatabaseProvider noteDatabaseProvider) {
    return Preconditions.checkNotNullFromProvides(ApplicationModule.INSTANCE.provideNoteRepository(noteDatabaseProvider));
  }
}
