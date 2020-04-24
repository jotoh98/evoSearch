package evo.search.io.entities;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class Project {
    @NonNull
    String version;
    @NonNull
    String path;
    @NonNull
    String name;

    List<Configuration> configurations = new ArrayList<>();
}
