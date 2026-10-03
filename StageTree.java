import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StageTree {
    HashMap<Path, ArrayList<StageItem>> entriesMap = new HashMap<>();

    public StageTree(List<String> entries) {
        entriesMap.put(Path.of("."), new ArrayList<>());

        for (var entry : entries) {
            var hash_path = entry.split(" ");
            var path = Path.of(hash_path[1]);
            var hash = hash_path[0];

            var parent_dir = path.getParent();
            var file = new StageFile(hash, path);
            verify_folder(parent_dir);
            entriesMap.get(parent_dir == null ? Path.of(".") : parent_dir).add(file);
        }

        System.out.println(entriesMap.toString());
    }

    void verify_folder(Path dir) {
        if (dir == null || dir.equals(Path.of(".")) || entriesMap.containsKey(dir))
            return;
        entriesMap.put(dir, new ArrayList<>());
        var parent_dir = dir.getParent();
        verify_folder(parent_dir);
        entriesMap.get(parent_dir).add(new StageFolder(dir));
    }

    interface StageItem {
        Path path();
    }

    record StageFile(String hash, Path path) implements StageItem {
        public String toString() {
            // return "blob " + hash + " " + path.toString();
            return path.toString();
        }
    }

    record StageFolder(Path path) implements StageItem {
    }
}
