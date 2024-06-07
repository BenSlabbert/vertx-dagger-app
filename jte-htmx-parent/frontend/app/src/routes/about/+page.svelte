<div id="response-div" hx-ext="response-targets">
	<button
		hx-post="http://localhost:8080/api/clicked?q=345"
		hx-swap="outerHTML"
		hx-target="#response-div"
		hx-target-404="#not-found"
		hx-target-500="#serious-errors"
	>
		Register!
	</button>
	<div id="serious-errors"></div>
	<div id="not-found"></div>
</div>
