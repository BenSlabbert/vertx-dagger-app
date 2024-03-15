@import com.example.jtehtmx.template.Page

@param Page page

<head>
    @if(page.getDescription() != null)
    <meta name="description" content="${page.getDescription()}">
    @endif
    <title>${page.getTitle()}</title>
</head>

<body>
    <h1>${page.getTitle()}</h1>
    <p>Welcome to my example page!</p>
</body>
