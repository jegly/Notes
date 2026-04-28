package com.kin.easynotes.domain.usecase;

import com.kin.easynotes.data.repository.ImportExportRepository;
import com.kin.easynotes.data.repository.NoteRepositoryImpl;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;

@ScopeMetadata
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
public final class ImportExportUseCase_Factory implements Factory<ImportExportUseCase> {
  private final Provider<NoteRepositoryImpl> noteRepositoryProvider;

  private final Provider<CoroutineScope> coroutineScopeProvider;

  private final Provider<ImportExportRepository> fileRepositoryProvider;

  private ImportExportUseCase_Factory(Provider<NoteRepositoryImpl> noteRepositoryProvider,
      Provider<CoroutineScope> coroutineScopeProvider,
      Provider<ImportExportRepository> fileRepositoryProvider) {
    this.noteRepositoryProvider = noteRepositoryProvider;
    this.coroutineScopeProvider = coroutineScopeProvider;
    this.fileRepositoryProvider = fileRepositoryProvider;
  }

  @Override
  public ImportExportUseCase get() {
    return newInstance(noteRepositoryProvider.get(), coroutineScopeProvider.get(), fileRepositoryProvider.get());
  }

  public static ImportExportUseCase_Factory create(
      Provider<NoteRepositoryImpl> noteRepositoryProvider,
      Provider<CoroutineScope> coroutineScopeProvider,
      Provider<ImportExportRepository> fileRepositoryProvider) {
    return new ImportExportUseCase_Factory(noteRepositoryProvider, coroutineScopeProvider, fileRepositoryProvider);
  }

  public static ImportExportUseCase newInstance(NoteRepositoryImpl noteRepository,
      CoroutineScope coroutineScope, ImportExportRepository fileRepository) {
    return new ImportExportUseCase(noteRepository, coroutineScope, fileRepository);
  }
}
