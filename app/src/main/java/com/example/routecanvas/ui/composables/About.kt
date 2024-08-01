package com.example.routecanvas.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routecanvas.ui.theme.RouteCanvasTheme

@Composable
fun About() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "About",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(15.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = "RouteCanvas:",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        HyperlinkText(
            text = "The motivation behind this app is to provide the same functionality, hopefully with the same design, as shown in this X.com post by @samdape.",
            linkText = listOf("post", "@samdape"),
            hyperlinks = listOf(
                "https://x.com/samdape/status/1808174105482436709", "https://x.com/samdape"
            ),
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = "Me:",
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        HyperlinkText(
            text = "I created this app because i liked the design. I don't have much to say about about me but you can find me on,\nGithub:@shivamparihar12 X:@sskaraxx Linkedin:@shivam-parihar",
            linkText = listOf("@shivamparihar12", "@sskaraxx","@shivam-parihar"),
            hyperlinks = listOf("https:://github.com/shivamparihar12", "https://x.com/sskaraxx","https://www.linkedin.com/in/shivam-parihar/"),
            fontSize = 15.sp
        )
    }
}

@Composable
fun HyperlinkText(
    modifier: Modifier = Modifier,
    text: String,
    linkText: List<String>,
    hyperlinks: List<String>,
    linkTextColor: Color = MaterialTheme.colorScheme.primary,
    linkTextFontWeight: FontWeight = FontWeight.Normal,
    linkTextDecoration: TextDecoration = TextDecoration.Underline,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontFamily: FontFamily = FontFamily.Monospace
) {
    val uriHandler = LocalUriHandler.current
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        linkText.forEachIndexed { index, link ->
            val startIndex = text.indexOf(link, lastIndex)
            val endIndex = startIndex + link.length
            if (startIndex > lastIndex) append(text.substring(lastIndex, startIndex))
            val linkUrL = LinkAnnotation.Url(
                hyperlinks[index], TextLinkStyles(
                    SpanStyle(
                        color = linkTextColor,
                        fontSize = fontSize,
                        fontWeight = linkTextFontWeight,
                        textDecoration = linkTextDecoration,
                        fontFamily = fontFamily
                    )
                )
            ) {
                val url = (it as LinkAnnotation.Url).url
                uriHandler.openUri(url)
            }
            withLink(linkUrL) { append(link) }
            append(" ")
            lastIndex = endIndex + 1
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
        addStyle(
            style = SpanStyle(
                fontSize = fontSize, fontFamily = fontFamily
            ), start = 0, end = text.length
        )
    }
    Text(text = annotatedString, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun HyperlinkTextPreview() {
    RouteCanvasTheme {
        HyperlinkText(
            text = "I hate building things. You can find me on @github.com/shivamparihar12 and @x.com/sskaraxx",
            linkText = listOf("@github.com/shivamparihar12", "@x.com/sskaraxx"),
            hyperlinks = listOf("https:://github.com/shivamparihar12", "https://x.com/sskaraxx")
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AboutPreview() {
    RouteCanvasTheme {
        About()
    }
}