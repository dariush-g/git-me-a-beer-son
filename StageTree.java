import java.io.IOException;
import java.nio.file.Files;
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

    // takes in the path at each line of the index. If the directory is already inside the map, it
    // returns, meaning the folder is already accounted for. Otherwise, it recursively checks the
    // parent directories, putting unaccounted folders in the map and adding the child folder to their list of items.
    void verify_folder(Path dir) {
        if (dir == null || dir.equals(Path.of(".")) || entriesMap.containsKey(dir))
            return;
        entriesMap.put(dir, new ArrayList<>());
        var parent_dir = dir.getParent();
        verify_folder(parent_dir);
        entriesMap.get(parent_dir).add(new StageFolder(dir));
    }

    String build_entry(StageFolder dir) throws IOException {
        var items = entriesMap.get(dir.path());

        var contents = new ArrayList<String>();
        for (var item : items) {
            if (item instanceof StageItem) {
                contents.add(item.toString());
            }
            if (item instanceof StageFolder) {
                var content = build_entry((StageFolder) item);
                var hash = HashFile.hashString(content);
                contents.add("tree " + hash + " " + item.path());

                Files.writeString(Path.of("./git/objects/" + hash), content);
            }
        }

        return String.join("\n", contents);
    }

    interface StageItem {
        Path path();
    }

    record StageFile(String hash, Path path) implements StageItem {
        public String toString() {
            return "blob " + hash + " " + path.toString();
        }
    }

    record StageFolder(Path path) implements StageItem {

    }
}
