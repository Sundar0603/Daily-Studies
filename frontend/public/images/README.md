# Topic Highlight Images

Place image files here to show as the background of the highest-count topic pill.

Expected filenames (any common format — .jpg, .png, .webp):

| Topic         | Filename                  |
|---------------|---------------------------|
| Technical     | `technical.jpg`           |
| Non-Technical | `non-technical.jpg`       |
| Investments   | `investments.jpg`         |
| Security      | `security.jpg`            |
| AI            | `ai.jpg`                  |
| Stocks        | `stocks.jpg`              |

If a file is missing the highlight pill will just show the dark overlay without an image.
To add a new topic image, drop the file here and add its entry to the `topicImages` map in `src/App.vue`.
